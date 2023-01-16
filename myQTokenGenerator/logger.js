require('colors');

class Logger {
    info(msg) {this.write(msg, "info");}
    error(msg) {this.write(msg, "error");}
    warn(msg) {this.write(msg, "warn");}

    write(message, level) {
        switch (level) {
            case "info":
                console.log(`Info: `.green + message)
                break;
            case "error":
                console.error(`Error: `.red + message);
                break;
            case "warn":
                console.log(`Info: `.yellow + message);
        }
    }
}

module.exports = Logger;