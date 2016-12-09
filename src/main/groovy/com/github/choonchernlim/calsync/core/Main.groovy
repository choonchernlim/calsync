package com.github.choonchernlim.calsync.core

/**
 * Runner class.
 */
class Main {
    static void main(String[] args) {
        new ExchangeToGoogleService(new UserConfig()).run()
    }
}