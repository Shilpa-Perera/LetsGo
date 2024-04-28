# Let'sGo - Indoor navigation using wifi fingerprinting

## Introduction

Positioning systems, or location-based services (LBS), have become increasingly popular with the rise of smartphones and mobile applications. While GPS is reliable outdoors, indoor environments pose challenges due to structural complexities and signal attenuations. Let'sGo indoor navigation app addresses these challenges by utilizing Wi-Fi fingerprinting alongside GPS to track indoor positions effectively.

## Use Cases

### Admin
- Add indoor location maps
- Add reference points

### User
- Activate mobile phone location service (GPS)
- Select an indoor map for navigation

## Architecture

The system consists of two main components:

1. **Admin Module**: After authentication, admins set up location maps and conduct site surveys to establish reference points by recording Wi-Fi signal strengths (RSS) and GPS coordinates.
2. **User Module**: Users' GPS coordinates determine available indoor maps within a 100m radius. Upon map selection, the app scans nearby Wi-Fi access points, compares RSS values with stored data, and displays the user's location.

## Tools & Algorithms Used

- **Android Studio Java**: Development environment
- **Android Location Services API**: Provided by Google Play Services for GPS location estimations
- **Android WiFi Manager**: For scanning nearby Wi-Fi access points
- **Firebase Firestore**: NoSQL database for storing reference points and map data
- **Firebase Storage**: Image store
- **KNN Algorithm**: Utilized for estimating user's location based on Wi-Fi signal strengths. KNN works by comparing signal strengths with a database of reference points and computing the weighted average of the nearest points.
- **Haversine Formula**: Used to calculate the distance between two geographical points specified by latitude and longitude coordinates.

## Assumptions and Challenges

- Consistency in RSS values is crucial for accuracy, but factors like interference and network fluctuations may cause variations.
- Admins require meticulously scaled and detailed maps for accurate mapping.

## UI

  

