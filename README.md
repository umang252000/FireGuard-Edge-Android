FireGuard Edge (Android On-Device Fire Detection System)
Theme: Early Hazard Detection / Safety & Security with Edge AI

1. Problem Statement

Fires remain one of the most destructive and unpredictable hazards worldwide.
Every year, millions of homes, shops, small businesses, and rural households experience fires caused by:

unattended cooking

short circuits

candles

gas leaks

flammable materials

Traditional fire detection systems (smoke alarms, CCTV monitoring, IoT sensors) suffer from major limitations:

High cost — professional systems cost ₹5,000–₹25,000

Late detection — smoke-only or flame-only systems trigger too late

False alarms — cooking steam, incense smoke, fog

Cloud dependence — require WiFi, internet, or expensive hardware

Inaccessibility in rural areas and low-income homes

There is a strong need for an affordable, reliable, offline early-warning system that anyone can deploy without hardware installation.

2. Objective of the Project

FireGuard Edge aims to create a fully on-device fire and smoke early-detection system using just an Android phone, powered by lightweight Edge Impulse models.

Primary objectives:

Detect fire and smoke visually using the phone’s camera.

Detect fire crackle or abnormal burning audio using the microphone.

Fuse multi-sensor AI outputs to maximize accuracy and prevent false alarms.

Run 24×7 on-device, even when the app is minimized, with no internet.

Provide instant alerts through alarm sound, vibration, notification, and logs.

Make fire safety accessible, deployable, and affordable for everyone.

FireGuard Edge turns any smartphone into a real-time fire early-warning device.

3. Methodology & Technical Implementation
A. Multi-Modal Data Acquisition

To improve robustness, the system captures two independent modalities:

Camera frames (Vision) — detection of fire & smoke patterns

Audio clips (Microphone) — detection of fire crackle / burning sound signatures

Data was collected using the Edge Impulse Mobile Client and augmented with open public datasets.

B. Model Design Using Edge Impulse
1. Vision Model (Fire/Smoke/Normal)

Input: 128×128 RGB frames

Model: MobileNetV2 (quantized)

Output: fire, smoke, normal

Goal: Detect visual flame/smoke cues in varied lighting

2. Audio Model (Fire Crackle / Ambient)

Input: 1-second audio windows

DSP: MFCC (40 bands)

Model: Tiny 1D CNN (quantized)

Output: fire_crackle, ambient

Both models were trained, validated, and exported using Edge Impulse Deployment (Android C++ library), enabling efficient on-device inference.

C. Fusion Logic (Core Innovation)

A reliable fire detection system must combine signals.
FireGuard Edge uses weighted fusion + hysteresis:

visionPositive = (vision == fire/smoke && conf > 0.6)
audioPositive = (audio == fire_crackle && conf > 0.6)

fusionScore = 0.6*vision_conf + 0.4*audio_conf

If fusionScore > 0.65 for 3 consecutive frames:
       trigger alarm


This dramatically reduces false positives from:

steam

incense smoke

shadows

background noise

sudden brightness changes

D. On-Device Android Implementation

The entire pipeline runs offline using:

CameraX for real-time video frames

AudioRecord for continuous audio capture

Edge Impulse C++ inference via JNI

Foreground Monitoring Service to monitor even when minimized

LocalBroadcastManager for real-time UI updates

Alerts include:

alarm sound

vibration

push notification

event logs

on-screen warning

No cloud API calls.
No backend needed.
Extremely low latency (<250ms end-to-end).

4. Scope & Impact of the Solution
A. Accessibility

Works on any basic Android phone. No special hardware required.

B. Safety-Critical Use Cases

Homes & kitchens

Hostels & dormitories

Small shops / godowns

Rural houses

Elderly & disabled individuals

Temporary construction sites

Camping & outdoor environments

C. Scalability

Can be deployed as:

A standalone app

A low-cost safety accessory

A community-alert system

A plug-in for smart home devices

A fire/hazard dataset collector

D. High Social Impact

A smartphone is the world’s most available computing device.
Turning it into a life-saving early fire detection tool can:

Reduce injuries and deaths

Prevent large property losses

Bring AI-powered safety to low-income communities

Increase awareness and real-time vigilance

5. Key Highlights & Innovation

Edge-only multi-modal AI (vision + audio)

Zero cloud dependency

Smart fusion + hysteresis filters false alarms

24×7 autonomous monitoring via background service

Lightweight, quantized models optimized for mobile CPUs

Scalable and extensible (thermal sensors, gas sensors optional)

Fast deployment — just install on any phone

6. Conclusion

FireGuard Edge demonstrates the power of Edge AI to solve real-world, high-impact problems using everyday devices.

By combining:

Multi-sensor inputs

Efficient Edge Impulse models

Background Android services

Fusion-based decision making

…it provides an affordable, accessible, and reliable early fire detection solution for millions of people across the world.

This project directly aligns with the theme of Safety & Security using Edge AI and showcases a practical, deployable, real-world application.

Built With

Edge Impulse
Android Studio
Kotlin
CameraX
AudioRecord API
JNI / NDK
C++ (Edge Impulse SDK)
TensorFlow Lite (quantized models)
Foreground Service
LocalBroadcastManager
Material Design Component
