# Jump
In the beginning, we were trying to build a jump-related game. Jump distance depends on the data read from the sensor when the user is carrying a sensor and making a physical jump. When the user jumps too far or too late will cause the failure of reaching the platform and then lose the game.

But after 3 days trying, we think we are not able to learn openGL and complete this game within the specified date. So we decided to change our project idea o build a running-related project. It can trace the user's running route and draw it on the map, and calculating user's bmi. We know that our idea is kind of conventional, but we will exert as much as knowledge points that we learned during this period.

Members:
Hao Zhang && Mikael Ramo

## Requirement
- Real phone
- Android Studio

## View demo video to help you understand code
[![Demo video](https://i.imgur.com/IPYnKJj.png)](https://youtu.be/qKfWnml2-UQ)


## Cautions and Project Structure
It requires GPS and Network permissions, the former one is used to locate yourself while running, the latter one is used to upload your running data to web database. And you need a Google Maps API key and paste your key into [google_maps_api.xml] (app/src/debug/res/values/google_maps_api.xml)
![Route](https://i.imgur.com/y9e5Pyx.png)

## Knowledge required and covered
![Route](https://i.imgur.com/9L9ta0N.png)

## Features
- GPS location, measure distance
- Draw real-time path on the map while running
- Lock screen using proximity internal sensor
- Upload data to web database
- data persistence using roomDatabase and LiveData
- Beautiful UI design
- And more...

## Screenshots
|                    Route                     |                  History               |                  BMI               |   
| ------------------------------------------- |--------------------------------------------|--------------------------------------------|
|![Route](https://i.imgur.com/OosAF8W.png)     |![History](https://i.imgur.com/94w2f4m.png)|![BMI](https://i.imgur.com/48yRGJe.png)|

## More
|                    Home                    |                  Details                   |                  DarkHome               |                  DarkDetails               |      
| ------------------------------------------- |--------------------------------------------|-----------------------------------------|-----------------------------------------|
|![Home](https://i.imgur.com/PJMS5N7.png)     |![Details](https://i.imgur.com/GYc5eC4.png) |![DarkHome](https://i.imgur.com/drVDn7c.png)|![DarkDetails](https://i.imgur.com/nLltlAS.png)|

## UI Documentation
More details can be found in [Here](https://drive.google.com/drive/u/1/folders/1Pis_Fp-WW-sY4XwzR1-8VR4oKVCQva-k?usp=sharing)

## Test
This app was tested successfully on Huawei cell phone (version 6.0) using Android Studio 3.1.1

## Thanks for viewing, Have a nice day :)
![Route](https://i.imgur.com/npIjq1g.jpg)
