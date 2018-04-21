

# Convoy Security Using Autonomous Quadcopter

![Final Video Link](https://github.com/hashimshafiq/BebopDroneProject/blob/master/images/1.png?raw=true)
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
 
 ### Work Progress Till Now
 
 | Features        | Implemented     | Tested          | Comment         |
 |:---------------:|:---------------:|:---------------:|:---------------:|
 | Manual Control  |  Done           | Yes             |Perfectly working|
 | Getting Stream  |  Done           | Yes             |Perfectly working|
 | H.264 to MAT    |  Done           | Yes             |Perfectly working|
 | Object Select/Lock|  Done         | Yes             |Perfectly working|
 | Pattern Learning|  Done           | Yes             |Perfectly Working|
 | CamShift tracking| Done           | Yes             |Perfectly Working|
 |Autonomous Rotation|Done           | Yes             |Perfectly Working|
 |Autonomous Movement|Done           | Yes             |Perfectly Working|
 | Altitude Maint.  | Done           | Yes             |Perfectly working|
 | Aruco Markers    | No             | No              |Removed from Proj|
 | CNN Object Detect| Done           | Yes             |Perfectly Working|
 | Object Tracking  | Done           | Yes             |Perfectly Working|
 
 
 ### ANDROID APP User Interface
 
 ![GUI](https://github.com/hashimshafiq/BebopDroneProject/blob/master/images/2.png?raw=true)
 
 
 
 ### Drone Testing Video Link
 
 
 [![Final Video](https://github.com/hashimshafiq/BebopDroneProject/blob/master/images/3.JPG?raw=true)](https://www.youtube.com/watch?v=iv0nGtHlEvM)
 
 
 
 
