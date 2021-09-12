/* Author: Brian Beaird
 *  This code was adapted from HJD's implementation (https://github.com/hjdhjd)
 *
 */

const pkceChallenge = require('pkce-challenge');
const htmlParser = require('node-html-parser');
const fetch = require('node-fetch');
const Logger = require("./logger.js");
const readline = require("readline");
const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const logger = new Logger();
var myqEmail;
var myqPassword;


const promptForCredentials = () => {
    rl.question('Enter your MyQ Email: ', (email) => {
        rl.question('Enter your MyQ Password: ', (password) => {
            myqEmail = email;
            myqPassword = password;
            rl.close();
            getToken();
        })
    })
}

const getToken = async () => {
    try {

      const MYQ_API_CLIENT_ID = "IOS_CGI_MYQ";
        const MYQ_API_CLIENT_SECRET = "VUQ0RFhuS3lQV3EyNUJTdw==";
        const MYQ_API_REDIRECT_URI = "com.myqops://ios";
        let pkceObj = pkceChallenge();

        const authEndpoint = new URL("https://partner-identity.myq-cloud.com/connect/authorize");
        authEndpoint.searchParams.set("client_id", "IOS_CGI_MYQ");

        authEndpoint.searchParams.set("code_challenge", pkceObj.code_challenge);
        authEndpoint.searchParams.set("code_challenge_method", "S256");
        authEndpoint.searchParams.set("redirect_uri", "com.myqops://ios");
        authEndpoint.searchParams.set("response_type", "code");
        authEndpoint.searchParams.set("scope", "MyQ_Residential offline_access");

        logger.info('Getting verification token...')

        // Send the PKCE challenge and let's begin the login process.
        const authPage = await fetch(authEndpoint.toString(), {
          headers: { "User-Agent": "null" }//,
          //redirect: "follow"
        }, true);

        // Grab the cookie for the OAuth sequence. We need to deal with spurious additions to the cookie that gets returned by the myQ API.
        const cookie = trimSetCookie(authPage.headers.raw()["set-cookie"]);

        // Parse the myQ login page and grab what we need.
        const htmlText = await authPage.text();
        const loginPageHtml = htmlParser.parse(htmlText);
        const requestVerificationToken = loginPageHtml.querySelector("input[name=__RequestVerificationToken]")?.getAttribute("value");

        if(!requestVerificationToken) {
            throw({message: 'Unable to retrieve verification token from login page.'})
        }


        // Set the login info.
        const loginBody = new URLSearchParams({ "Email": myqEmail, "Password": myqPassword, "__RequestVerificationToken": requestVerificationToken });
        logger.info('Logging into MyQ...')

        // Login and we're done.
        const loginResponse = await fetch(authPage.url, {
          body: loginBody.toString(),
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Cookie": cookie,
            "User-Agent": "null"
          },
          method: "POST",
          redirect: "manual"
        }, true);

        let redirectUrl = loginResponse.headers.get("location")
        if (!redirectUrl){
            throw({message: 'MyQ login failed with email/password combination.'})
        }

        // Cleanup the cookie so we can complete the login process by removing spurious additions
        // to the cookie that gets returned by the myQ API.
        const redirectCookie = trimSetCookie(loginResponse.headers.raw()["set-cookie"]);

        // Execute the redirect with the cleaned up cookies and we're done.
        logger.info('Getting oauth access code...')
        const redirectResponse = await fetch(redirectUrl, {
          headers: {
            "Cookie": redirectCookie,
            "User-Agent": "null"
          },
          redirect: "manual"
        }, true);

        const redirectResponseUrl = new URL(redirectResponse.headers.get("location") ?? "");
        if (!redirectResponseUrl){
          throw({message: 'Oauth redirect failed.'})
        }

        // Create the request to get our access and refresh tokens.
        const tokenRequestBody = new URLSearchParams({
          "client_id": MYQ_API_CLIENT_ID,
          "client_secret": Buffer.from(MYQ_API_CLIENT_SECRET, "base64").toString(),
          "code": redirectResponseUrl.searchParams.get("code"),
          "code_verifier": pkceObj.code_verifier,
          "grant_type": "authorization_code",
          "redirect_uri": MYQ_API_REDIRECT_URI,
          "scope": redirectResponseUrl.searchParams.get("scope")
        });

        // Now we execute the final login redirect that will validate the PKCE challenge and
        // return our access and refresh tokens.
        logger.info('Requesting access tokens')
        let tokenResponse = await fetch("https://partner-identity.myq-cloud.com/connect/token", {
          body: tokenRequestBody.toString(),
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "User-Agent": "null"
          },
          method: "POST"
        }, true);

        const token = await tokenResponse.json();
        logger.info('Success! The token below has been copied to your clipboard. Paste it into the MyQToken app setting of the SmartApp.')
        require('child_process').spawn('clip').stdin.end(token.refresh_token)
        logger.info(token.refresh_token)
      } catch (error) {
        logger.error(error.message);
      }
      finally{
        pauseAtEnd();
      }
}

function pauseAtEnd(){
    rl.question('', (key) => {
      rl.close();
      process.exit(0);
  })
}

function trimSetCookie(setCookie){

    // We need to strip spurious additions to the cookie that gets returned by the myQ API.
    return setCookie.map(x => x.split(";")[0]).join("; ");
  }

promptForCredentials();