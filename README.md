# ProjectCarbon    <a href="https://creativecommons.org/licenses/by-sa/4.0/"><img align="right" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png"></a>
ProjectCarbon is an API designed to combine as many database formats as possible into one API for Java, allowing for conversions, less learning curves, and efficent development.

### Maven
```
<repositories>
        <repository>
            <id>funkemunky-repo</id>
            <url>http://funkemunky.cc:8080/nexus/content/repositories/releases/</url>
        </repository>
</repositories>

<dependencies>
        <dependency>
            <groupId>cc.funkemunky.utils</groupId>
            <artifactId>ProjectCarbon</artifactId>
            <version>1.0</version>
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

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon carbon;

    public static void main(String[] args) {
        carbon = new Carbon();
    }
}
```

## Creating Databases

### Flatfile
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon carbon;

    public static void main(String[] args) {
        carbon = new Carbon();

        //Defaults to the system user.home directory.
        carbon.createFlatfileDatabase("Test1");

        carbon.createFlatfileDatabase("/my/directory", "Test2");
    }
}
```

### MySQL
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon carbon;

    public static void main(String[] args) {
        carbon = new Carbon();

        carbon.createSQLDatabase("test", "localhost", "username", "password", 3306);
    }
}
```

### Mongo
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    private static Carbon carbon;

    public static void main(String[] args) {
        carbon = new Carbon();

        //Method one
        carbon.initMongo("localhost", 27017, "Carbon", "username", "password");
        carbon.createMongoDatabase("Test1");
        
        //Method Two;
        carbon.createMongoDatabase("Test2", carbon.initMongo("localhost", 27017, "Carbon", "username", "password"));
    }
}
```

## Accessing and Writing To Databases
We are using a Flatfile database for this example. It will be the same for any database type you use.

### Getting a database
```java
Database database = carbon.getDatabase("Test");
```

### Writing to and Getting From a database
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.Database;

public class Zenbans {

    private static Carbon carbon;

    public static void main(String[] args) {
        carbon = new Carbon();
        
        carbon.createFlatfileDatabase("Test");
        
        Database database = carbon.getDatabase("Test");

        database.inputField("example", "my file sucks.");

        System.out.println((String) database.getField("example"));
    }
}
```



This project is distributed under a <a href="https://creativecommons.org/licenses/by-sa/4.0/"> Creative Commons License</a> ("License"), and by using, viewing, or downloading this software, you agree to said License.




