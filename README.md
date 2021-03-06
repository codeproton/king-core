# king-core
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/d4064359a7ee4e3fa47571ccc3db7f49)](https://www.codacy.com/gh/codeproton/king-core?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=codeproton/king-core&amp;utm_campaign=Badge_Grade)
[![](https://jitpack.io/v/codeproton/king-core.svg)](https://jitpack.io/#codeproton/king-core)

Module's ecosystem and DI (Dependency Injection) 

## Summary
* [System requirements](#system-requirements)
* [Getting started](#getting-started)
* [Documentation](#documentation)
* [Development](#development)
* [Wiki](#wiki)
* [Contribution](#contributing)

## System requirements
To run it is necessary that you have the following contents installed:
  * [JRE](https://www.java.com/pt_BR/download/) 8 or higher.

## Getting-started
Download the latest version of plugin on [releases](https://github.com/codeproton/king-core/releases/latest) and move it to `/plugins/` folder.
The modules are loaded from `/plugins/KCore/modules`.

## Documentation
You can see the project documentation [here](https://codeproton.github.io/king-core)

## Development
Using github packages: 

Maven:
```xml
<dependencies>
    <dependency>
      <groupId>io.king.module</groupId>
      <artifactId>core</artifactId>
      <version>VERSION</version>
    </dependency>
</dependencies>
```
> Later use mvn install

Using jitpack: 
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.codeproton</groupId>
        <artifactId>king-core</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

If you're a developer and wants to develop a module to ecosystem, you can see examples on wiki or in the examples bellow.

> To start you need to create the configuration from your module
```java
import io.king.core.api.module.ModuleConfig;

public final class ModuleInformation extends ModuleConfig {

    public ModuleInformation() {
        super("ModuleName", "author's name", "A simple example", null);
    }
}
```

> Now you can create your module
```java
import io.king.core.api.cycle.LifeContext;
import io.king.core.api.cycle.LifeCycle;
import io.king.core.api.module.Module;

@Module(config = ModuleInformation.class)
public final class ModuleExample extends LifeCycle {
    
    @Override
    public void init(LifeContext context) {
        context.getLogger().info("Module example has been loaded!");
    }
}
```

## Wiki
do you wanna see more examples? Visit the [wiki](https://github.com/codeproton/king-core/wiki)

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[MIT](https://choosealicense.com/licenses/mit/)