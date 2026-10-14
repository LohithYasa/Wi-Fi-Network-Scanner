# Wi-Fi Network Scanner

A Java-based Wi-Fi Network Scanner for Windows that helps users discover and inspect nearby wireless networks through a simple command-line interface.

## Overview

The Wi-Fi Network Scanner is a networking project developed using Java. It uses Windows WLAN command-line utilities to retrieve nearby wireless access point information and provides features for searching, sorting, reviewing, and exporting discovered networks.

## Features

- **Wi-Fi Network Discovery:** Scan nearby wireless networks.
- **Network Information:** Display SSID, BSSID, signal strength, radio type, channel, authentication, and encryption.
- **Signal Strength Sorting:** Sort discovered networks by signal strength.
- **SSID Search:** Find networks using their names or partial names.
- **Scan History:** View network entries collected during the current application session.
- **Wi-Fi Interface Inspection:** Display available wireless interface information.
- **CSV Export:** Export collected network data to a CSV file for further analysis.
- **Error Handling:** Handle command failures, interruptions, and command timeouts.

## Technologies Used

- Java
- Java Collections Framework
- ProcessBuilder
- Windows `netsh wlan` commands
- File I/O and CSV export

## Requirements

- Windows operating system
- Java Development Kit (JDK)
- A working and enabled Wi-Fi adapter

## Installation and Execution

1. Install a JDK on your Windows computer.
2. Download or clone this repository.
3. Open Command Prompt in the project folder.
4. Compile the program:

   ```bash
   javac WiFiNetworkScanner.java
   ```

5. Run the application:

   ```bash
   java WiFiNetworkScanner
   ```

6. Select an option from the displayed menu.

## Menu Options

1. Scan nearby Wi-Fi networks
2. Sort networks by signal strength
3. Search network by SSID
4. View scan history
5. Inspect Wi-Fi interface
6. Export networks to CSV
7. Exit

## Output

The application displays discovered network details in a tabular format. When CSV export is selected, the results are saved to `wifi_networks.csv` in the application's working directory.

## Platform Compatibility

This implementation is designed for Windows because it uses the Windows `netsh wlan` utility. The network parser expects English-language Windows command output.

## Learning Objectives

- Understand Java process execution and command-line utilities.
- Work with Java classes, objects, collections, and sorting.
- Process text output and extract structured network information.
- Handle exceptions and command timeouts.
- Write structured data to CSV files.

## Disclaimer

This project is intended for educational purposes and authorized network discovery. Use it only on systems and networks you are permitted to inspect. It does not crack Wi-Fi passwords or bypass wireless network security.

## Author

Developed as a Java networking and cybersecurity learning project.
