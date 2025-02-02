Coin Flipper using Random Oracle
---------------------------------

A demo of using the Randomness Oracle Beacon Application to allow betting on a coin flip.


## Background

With the release of AVM7 including the `vrf_verify` opcode, a smart contract can take in some proof computed through a VRF process off chain and verify that it was computed honestly.

The application that verifies and stores the randomness can be treated as an oracle and other applications may call it to get the random value for a given round.

The Oracle application in this example is on testnet with app id `115885218` and adheres to [ARC-21](https://arc.algorand.foundation/ARCs/arc-0021)

To read more about how VRF works and best practices when dealing with on-chain randomness, please see [this post](https://developer.algorand.org/articles/usage-and-best-practices-for-randomness-beacon/)


## Demo
To run the demo, in either Python or Typescript, clone down the repo and `cd` into it
```sh
$ git clone git@github.com:algorand-devrel/coin-flipper.git
$ cd coin-flipper
```

### Back End 

To run the demo with python, create a virtual environment, source it, and install requirements

```
$ cd contracts
$ python -m venv .venv
$ source .venv/bin/activate
(.venv) $ pip install -r requirements.txt
```

Edit the main.py file to add your funded testnet account's mnemonic and run the program

```sh
(.venv) $ python main.py
```

This will create the application, fund it, opt your user in, and call the coin flip endpoint (choosing heads) then after some rounds will try to settle the bet.

### Front End 

To run the demo with the front end, cd into the `frontend` directory and install the requirements

```sh
$ cd frontend 
$ yarn
$ yarn run dev
```

Log in with a funded testnet account and go through the steps to create, opt in, flip, settle

### Android

To run the the demo in an Android emulator, cd into the `android` directory and load up the project in Android Studio.

Log in with a funded testnet account and go through the steps to opt in, flip, and settle.
