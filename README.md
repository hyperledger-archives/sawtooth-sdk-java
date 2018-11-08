![Hyperledger Sawtooth](https://raw.githubusercontent.com/hyperledger/sawtooth-core/master/images/sawtooth_logo_light_blue-small.png)

# Hyperledger Sawtooth Java SDK

## Install Sawtooth Java SDK from Maven Central Repository

#### Build a Sawtooth transaction processor

```xml
    <dependency>
        <groupId>org.hyperledger.sawtooth</group>
        <artifactId>sawtooth-sdk-transaction-processor</artifactId>
        <version>v0.1.2</version>
    </dependency>
```

#### Sign and verify signatures of transaction and batch headers

```xml
    <dependency>
        <groupId>org.hyperledger.sawtooth</groupId>
        <artifactId>sawtooth-sdk-signing</artifactId>
        <version>v0.1.2</version>
    </dependency>
```

#### Send messages to the Sawtooth validator interfaces (infrequent use case)

```xml
    <dependency>
        <groupId>org.hyperledger.sawtooth</groupId>
        <artifactId>sawtooth-sdk-protos</artifactId>
        <version>v0.1.2</version>
    </dependency>
```

## Examples (sawtooth-sdk-java/examples)
* xo_java
    - [transaction family specification](https://sawtooth.hyperledger.org/docs/core/releases/latest/transaction_family_specifications/xo_transaction_family.html)
* intkey_java
    - [transaction family specification](https://sawtooth.hyperledger.org/docs/core/releases/latest/transaction_family_specifications/integerkey_transaction_family.html)
* xo_android_client
    - An Android client for the XO transaction family, written in Kotlin


## Work on the Sawtooth SDK

#### Build the Sawtooth SDK

Requirements:
* Maven 3

Pull requests against the repo at [https://github.com/hyperledger/sawtooth-sdk-java](https://github.com/hyperledger/sawtooth-sdk-java)
are automatically built using the Jenkinsfile in the repository.

Important build steps in the Jenkinsfile:
* docker-compose -f docker/compose/java-build.yaml up (**builds the SDK and examples**)
* Run *docker-compose up* on each of the test yaml files in intkey_java/tests/* and xo_java/tests/*

#### Make A Pull Request

Follow the guidelines in the [Contributing documentation](https://sawtooth.hyperledger.org/docs/core/releases/latest/community/contributing.html) for making a pull request.
