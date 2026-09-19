# 3D Model Viewer

Android screening task built using Kotlin, Jetpack Compose, MVVM, SceneView and Filament.

## Tech Stack
- Kotlin
- Jetpack Compose
- MVVM
- SceneView
- Google Filament
- Min SDK 24

## Features
- Add any of the 5 bundled GLB models
- Multiple models displayed simultaneously
- One-finger drag moves the container
- Pinch resizes the container
- Interaction mode:
  - One-finger drag rotates the model
  - Two-finger pinch zooms the model
- Labels parsed from `nodes[].extras.prop`
- Labels follow 3D nodes using world-to-screen projection
- Connector lines between labels and model parts
- Independent close, interaction and label controls

## Why SceneView
SceneView provides a Compose-friendly layer over Filament and simplifies loading and rendering GLB models while still providing access to Filament entities, transforms and camera projection.

## Performance Optimizations
- Label projection only runs while labels are visible
- Label UI state updates only when projected coordinates meaningfully change
- Animations disabled where not required
- Models removed from Compose state when closed
- Temporary debug logging removed
- Rendering and gesture state kept local to avoid unnecessary ViewModel updates during high-frequency interactions

## Trade-offs
- SceneView was chosen for development speed and direct GLB support
- Label collision avoidance is simple and can be improved
- Each model is rendered independently, which simplifies interaction but has higher rendering cost

## Improvements With More Time
- Shared Filament rendering resources where practical
- Better label collision detection and placement
- More extensive performance profiling
- Automated tests for GLB metadata parsing
- Adaptive quality settings for very low-end devices

## Known Limitations
- Labels can overlap in some model orientations
- Performance depends on device GPU capability and model complexity

## Tested On
- Device: <your device/emulator>
- Android version: <version>

## Build
Open the project in Android Studio and run the `app` module.
