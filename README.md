# Convoy Security Using Autonomous Quadcopter

### Team Memebers
* Muhammad Hashim Shafiq (SP14-BCS-142)

* Ahtasham Ul Hassan (SP14-BCS-143)

* Zubair Baqai (SP14-BCS-122)

### Supervised by

   Dr. Mobeen Ghafoor
   
   COMSATS Institute of Information Technology Islamabad Pakistan 


### How project is organized ?

![UML](https://raw.githubusercontent.com/Parrot-Developers/Samples/master/Android/uml/mobile_uml_classes.png "UML Bebop Drone Sample")


### Common Error on Windows regarding opencv & android

App crashed due to __No implementation found for org.opencv.core.Mat.n_Mat()....__ error.
#### Solution
 * create folder `jniLibs` in `/app/src/main`
 * Copy all files from `opencv-sdk/sdk/native/libs` to `jniLibs` folder
 * Now Error goes
 
 ### Features added
 * Manual Control
 * Decode and convert video to Mat object
 * Image/video Enhancement 
 * Bounding box created
 
 ### Work Progress Till Now
 
 | Features        | Implemented     | Tested          | Comment         |
 |:---------------:|:---------------:|:---------------:|:---------------:|
 | Manual Control  |  Done           | Yes             |Perfectly working|
 | Getting Stream  |  Done           | Yes             |Perfectly working|
 | H.264 to MAT    |  Done           | Yes             |Perfectly working|
 
 
