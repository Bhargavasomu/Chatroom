# Chatroom
This project basically implements a client server model for communication amongst multiple connected clients in Java.

# Instructions
This is a simple Multiple Clients Single Server System. It involves the usage of threads

## System Requirements
1) Preferably Unix system for both client and server
2) Java 8

## Running The System
1) Go to the bin directory for turning on either Server or Client
2) Command to turn on the server : "java Server_main <maxNumClients>"
3) Command to turn on the client : "java Client_main <Client IP Address> <Server IP Address>"
4) For local testing, instead of the actual IP addresses, you can just use 127.0.0.1
5) For the user to exit the app, he can enter "exit" or "Exit" or "cntrl-C"

## Recompiling the System
1) Go to bin directory
2) Type the following command in the terminal - "bash run.sh"

## Features
1)  "list all users"					- lists all the users connected to the server
2)  "create chatroom <chatroomName>"			- creates a chatroom in the server
3)  "list chatrooms"					- lists all the chatrooms present in the server
4)  "join <chatroomName>"				- join an existing chatroom
5)  "add <userName>"					- add a user to the existing chatroom, in which you are present
6)  "leave"						- leave the chatroom in which user is present
7)  "list users"					- lists all the users present in the chatroom
8)  "reply "<msg>""					- send the message to all the users of chatroom via TCP
9)  "reply <fileName> tcp"				- send the file to all the users of chatroom via TCP
10) "reply <fileName> udp"				- send the file to all the users of chatroom via UDP
11) Chatroom gets destroyed when the users in it are 0
12) Creating a chatroom automatically adds that user to the created chatroom

## Constraints
1) Username should not have ';' present in it
2) Messages are broadcasted via only TCP
3) Files can be broadcasted via TCP or UDP optionally

## Details about the Client
Each client is associated with 3 threads. They are:
	a) Main thread for receiving inputs from user interacting with the server
	b) Server thread for TCP (To receive broadcast msgs and files through TCP)
	c) Server thread for UDP (To receive broadcast msgs and files through UDP)

## Details about the Server
Each client is supported by a miniserver created. The miniserver is the ClientHandler.
Each ClientHandler is created by creating threads

## Ports Hardcoded
1) 5432 is the port on server responsible for listening TCP msgs or files
2) 5433 is the port on server responsible for listening UDP msgs or files
