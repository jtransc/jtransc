package com.jtransc.debugger.jvm

// https://docs.oracle.com/javase/1.5.0/docs/guide/jpda/jdwp-spec.html

// Command Packet
// Header
// length (4 bytes)
// id (4 bytes)
// flags (1 byte)
// command set (1 byte)
// command (1 byte)
// data (Variable)

// Reply Packet
// Header
// length (4 bytes)
// id (4 bytes)
// flags (1 byte)
// error code (2 bytes)
// data (Variable)

