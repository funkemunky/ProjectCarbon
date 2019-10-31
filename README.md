# ProjectCarbon    <a href="https://creativecommons.org/licenses/by-sa/4.0/"><img align="right" src="https://i.creativecommons.org/l/by-sa/4.0/88x31.png"></a>
ProjectCarbon is an API designed to combine as many database formats as possible into one API for Java, allowing for conversions, less learning curves, and efficent development.

### JavaDocs
https://funkemunky.github.io/ProjectCarbon/

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
            <version>1.2.2</version>
        </dependency>
</dependencies>
```

### Gradle
```
repositories {
    mavenCentral()
    maven {
        name = 'funkemunky-repo'
        url = "http://nexus.funkemunky.cc/content/repositories/releases/"
    }
}

dependencies {
    compile group: 'cc.funkemunky.utils', name: 'ProjectCarbon', version: '1.2.2'
}
```

### What is ProjectCarbon?
This project is an API meant to simplify communicating with the many different database software solutions that are used everywhere. It allows for higher efficiency in development, decreased troubleshooting, and no room for accidental security loopholes caused by human error. Everything is done for developers.

### Initializing Carbon
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    public static void main(String[] args) {
        Carbon.init();
    }
}
```

## Creating Databases

### Flatfile
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    public static void main(String[] args) {
        Carbon.init();
        //Defaults to the system user.home directory.
        FlatfileDatabase database = new FlatfileDatabase("test");
        database.loadDatabase();
    }
}
```

If your Flatfile database was created with a version of ProjectCarbon before v1.2.2, you can convert it to the modern format like the example below.

```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    public static void main(String[] args) {
        Carbon.init();
        
        String directory = "myDirectory";
        File file = new File(directory, getName() + ".txt");

        //Defaults to the system user.home directory.
        FlatfileDatabase database = new FlatfileDatabase("test");
        
        database.loadDatabase(); //If your database was created with a legacy version, this will not import anything from it.
        
        database.convertFromLegacy(file); //This will set everything up and will only need to be run once.
    }
}
```

### MySQL
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    public static void main(String[] args) {
        Carbon.init();

        //This only needs to be set once unless you want to change the credentials.
        //It stores the credentials as a static operator, so all databases will be able to access it.
        MySQLDatabase.setCredentials("localhost", "root", "password");
        
        MySQLDatabase database = new MySQLDatabase("test");
        database.loadDatabase();
    }
}
```

### Mongo
```java
package cc.funkemunky.bans;

import cc.funkemunky.carbon.Carbon;

public class Zenbans {

    public static void main(String[] args) {
        Carbon.init();
        
        MongoDatabase database = new MongoDatabase("localhost", 27017, "testDB", "admin", "password");
    }
}
```

## Accessing and Writing To Databases
We are using a Flatfile database for this example. It will be the same for any database type you use.

### Getting a database
If you ever want to grab a database without having to directly access the field you where you initialize it,
you can use this method here.
```java
//Getting Flatfile
FlatfileDatabase flatfile = Carbon.INSTANCE.getDatabase("test");

//Getting Mongo
MongoDatabase mongo = Carbon.INSTANCE.getDatabase("testDB");

//Getting MySQL
MySQLDatabase mysql = Carbon.INSTANCE.getDatabase("test");
```

### Writing to and Getting From a database
ProjectCarbon converts every different database structure into one object called the "StructureSet". Inside these StructureSets includes a map of objects able to be grabbed with their respective keys. The code snippets provided below exemplifies the general use of this system.

```java
package dev.brighten.ex;

import cc.funkemunky.carbon.Carbon;
import cc.funkemunky.carbon.db.StructureSet;
import cc.funkemunky.carbon.db.flatfile.FlatfileDatabase;
import cc.funkemunky.carbon.utils.Pair;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Example {

    private static FlatfileDatabase database;
    private static ScheduledExecutorService schedular;
    public static void main(String[] args) {
        Carbon.init();

        database = new FlatfileDatabase("test");
        database.loadDatabase();

        schedular = Executors.newSingleThreadScheduledExecutor();

        //It is good practice to run a task repeatedly to save the database.
        //This helps prevent any data loss.
        schedular.scheduleAtFixedRate(database::saveDatabase,
                2, 5, TimeUnit.MINUTES);

        StructureSet set;
        if(database.contains("test-set")) {
            set = database.get("test-set");
            System.out.println("Field: " + set.getField("test-field"));
            set.inputField("test-field", 0);
        } else set = database.createStructure("test-set",
                new Pair<>("test-field", 1)); //This will add the set to database cache.
        database.updateObject(set); //This will insert the set, or update any existing set.

        System.out.println("Field (2): " + set.getField("test-field"));

        //I am only saving the database because I immediately shutdown the program.
        //You should always save on potential crashes or shutdowns.
        database.saveDatabase();
        System.exit(0);;
    }
}
```
The project containing this class is able to be bound in the "Example" module folder in the source code above.



This project is distributed under a <a href="https://creativecommons.org/licenses/by-sa/4.0/"> Creative Commons License</a> ("License"), and by using, viewing, or downloading this software, you agree to said License.
