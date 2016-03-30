# Ruqus

[Readme is still a WIP, sorry!]

## Installation
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    apt 'com.bkromhout.ruqus:ruqus-compiler:1.0.0'
    compile 'com.bkromhout.ruqus:ruqus-core:1.0.0'
}
```
