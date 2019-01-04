========================
Importing the Java SDK
========================

.. note::
   The Sawtooth Java SDK requires JDK 8 or above.

Once you've got a working version of Sawtooth, there are a few additional
steps you'll need to take to get started developing for Sawtooth.

**Java Project**

Add the Sawtooth Signing and Proto SDK to your ``pom.xml`` file.

.. code-block:: xml

    <dependency>
      <groupId>org.hyperledger.sawtooth</groupId>
      <artifactId>sawtooth-sdk-signing</artifactId>
      <version>v0.1.2</version>
    </dependency>
    <dependency>
      <groupId>org.hyperledger.sawtooth</groupId>
      <artifactId>sawtooth-sdk-protos</artifactId>
      <version>v0.1.2</version>
    </dependency>

**Android Project**

1. Add Maven central as a repository to your project. On `build.gradle` add:

.. code-block:: ini

    allprojects {
      repositories {
          ...
          mavenCentral()
      }
    }

2. Add Sawtooth Signing and Proto SDK as a dependency. On `build.gradle` add:

.. code-block:: ini

    dependencies {
      implementation 'org.hyperledger.sawtooth:sawtooth-sdk-signing:v0.1.2'
      implementation 'org.hyperledger.sawtooth:sawtooth-sdk-protos:v0.1.2'
      ...
    }

.. Licensed under Creative Commons Attribution 4.0 International License
.. https://creativecommons.org/licenses/by/4.0/
