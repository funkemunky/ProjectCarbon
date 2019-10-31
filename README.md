# ProjectCarbon    <a href="https://creativecommons.org/licenses/by-sa/4.0/"><img align="right" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png"></a>
ProjectCarbon is an API designed to combine as many database formats as possible into one API for Java, allowing for conversions, less learning curves, and efficent development.

### Maven
```
<repositories>
        <repository>
            <id>funkemunky-repo</id>
            <url>http://nexus.funkemunky.cc/content/repositories/releases/</url>
        </repository>
</repositories>

<dependencies>
        <dependency>
            <groupId>cc.funkemunky.utils</groupId>
            <artifactId>ProjectCarbon</artifactId>
            <version>1.1.1</version>
        </dependency>
</dependencies>
```

### Gradle
```
repositories {
    mavenCentral()
    maven {
        name = 'funkemunky-repo'
        url = "http://funkemunky.cc:8080/nexus/content/repositories/releases/"
    }
}

dependencies {
    compile group: 'cc.funkemunky.utils', name: 'ProjectCarbon', version: '1.0'
}
```

### What is ProjectCarbon?
This project is an API meant to simplify communicating with the many different database software solutions that are used everywhere. It allows for higher efficiency in development, decreased troubleshooting, and no room for accidental security loopholes caused by human error. Everything is done for developers.

### Initializing Carbon
```java
package cc.funkemunky.bans;

import cc.funkemunky.cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon cc.funkemunky.carbon;

    public static void main(String[] args) {
        cc.funkemunky.carbon = new Carbon();
    }
}
```

## Creating Databases

### Flatfile
```java
package cc.funkemunky.bans;

import cc.funkemunky.cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon cc.funkemunky.carbon;

    public static void main(String[] args) {
        cc.funkemunky.carbon = new Carbon();

        //Defaults to the system user.home directory.
        cc.funkemunky.carbon.createFlatfileDatabase("Test1");

        cc.funkemunky.carbon.createFlatfileDatabase("/my/directory", "Test2");
    }
}
```

### MySQL
```java
package cc.funkemunky.bans;

import cc.funkemunky.cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon cc.funkemunky.carbon;

    public static void main(String[] args) {
        cc.funkemunky.carbon = new Carbon();

        cc.funkemunky.carbon.createSQLDatabase("test", "localhost", "username", "password", 3306);
    }
}
```

### Mongo
```java
package cc.funkemunky.bans;

import cc.funkemunky.cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon cc.funkemunky.carbon;

    public static void main(String[] args) {
        cc.funkemunky.carbon = new Carbon();

        //Method one
        cc.funkemunky.carbon.initMongo("localhost", 27017, "Carbon", "username", "password");
        cc.funkemunky.carbon.createMongoDatabase("Test1");
        
        //Method Two;
        cc.funkemunky.carbon.createMongoDatabase("Test2", cc.funkemunky.carbon.initMongo("localhost", 27017, "Carbon", "username", "password"));
    }
}
```

## Accessing and Writing To Databases
We are using a Flatfile database for this example. It will be the same for any database type you use.

### Getting a database
```java
Database database = cc.funkemunky.carbon.getDatabase("Test");
```

### Writing to and Getting From a database
```java
package cc.funkemunky.bans;

import cc.funkemunky.cc.funkemunky.carbon.Carbon;
import cc.funkemunky.cc.funkemunky.carbon.db.Database;

public class Zenbans {

    private static Carbon cc.funkemunky.carbon;

    public static void main(String[] args) {
        cc.funkemunky.carbon = new Carbon();
        
        cc.funkemunky.carbon.createFlatfileDatabase("Test");
        
        Database database = cc.funkemunky.carbon.getDatabase("Test");

        database.inputField("example", "my file sucks.");

        System.out.println((String) database.getField("example"));
    }
}
```



This project is distributed under a <a href="https://creativecommons.org/licenses/by-sa/4.0/"> Creative Commons License</a> ("License"), and by using, viewing, or downloading this software, you agree to said License.




