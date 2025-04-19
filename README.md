# Scheduler App

## Overview

Scheduler App allows users to schedule, manage, and track application executions on their Android device. With an intuitive interface and color-coded states, users can easily monitor the status of their scheduled tasks.

## Features

- **Schedule Applications**: Set specific times for apps to execute
- **Manage Schedules**: Cancel or modify existing schedules as needed
- **Time Conflict Detection**: Set at most 11 apps to execute at a specific time. Get alerts when attempting to schedule more than 11 apps at the same time
- **Execution History**: View complete execution history for each application
- **Visual Status Tracking**: Color-coded states make it easy to identify schedule status at a glance

## Schedule States

The app uses a simple color-coding system to represent different schedule states:

- **GRAY**: Not Scheduled
- **BLUE**: Scheduled
- **RED**: Canceled
- **GREEN**: Executed

## Usage

1. **Schedule an App**: Select an app and set your desired execution time
2. **Cancel a Schedule**: Find the scheduled app and tap the cancel option
3. **Modify Schedule Time**: Select an existing schedule and adjust the time
4. **View Execution History**: Tap the "Executions" button on any app to see its execution history

## Technical Details

Built with Kotlin for Android

## Requirements

- Android 10.0 or later
- Permission to access installed applications and draw over other app permission.