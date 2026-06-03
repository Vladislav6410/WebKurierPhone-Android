# WebKurierPhone-Android Architecture

## Role

Level 3 Android Client.

## Responsibilities

- native Android UI
- API client
- secure local storage
- user interaction
- connection to Core and PhoneCore

## Must NOT

- contain business logic
- store WebCoin ledger
- control drones
- replace PhoneCore
- replace Core

## Boundary

Android is a client only.