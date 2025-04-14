# gps-stop-detector
In Project 3, we will continue working with our triplog dataset. This dataset contains latitude and longitude data that was gathered over a 3 day trip. Last project, we created some useful methods to help us find aggregate data. This project, we want to further refine our data by removing excess points that are grouped together, which we will call "stops". You will create a new ArrayList which will not include the points that are stops, so that we can get a more accurate sense of the trip data, such as total time and average speed. 

In case you were unable to complete project 2, a starting template for the TripPoint class is included in the repo. You don't have to use it if you finished your own. You should document the methods in the template if you decide to use it. 

## Defining Stops

We need to define some heuristics for determining when a stop occurs. You will be implementing stop detection for the following two heuristics: 

1) The first heuristic for detecting stop zones is to check if a point is within a certain distance from the previous point. We will call this distance the "displacement threshold". If a point falls within the displacement threshold of the previous point, it should be considered a stop.

2) The second heuristic for detecting stop zones is to check for groups of three or more points that are all within a certain distance of each other. We will call this distance the "stop radius". If there are three or more points within the stop radius of each other, we consider them to be part of the same stop zone. This way the points do not have to be directly consecutive. For example, a point could be outside the stop radius of the point directly before it, but inside the radius of another point in the same stop zone, so that point is still considered a stop. 

Note that "within" the distance here means it is inclusive (less than or equal to). 

## TripPoint UML

<img src=./resources/UML2.PNG width=50% height=50%>

Feel free to add your own helper methods as needed. 

## Old Methods (These are still the same)

### Constructors

`TripPoint(int time, double lat, double lon)`: Initialize the class fields `time`, `lat`, and `lon`.

### Getters

`getTime()`: Should return `time`.

`getLat()`: Should return `lat`.

`getLon()`: Should return `lon`.

`getTrip()`: Should return a copy of the `trip` ArrayList

### Other Methods

`readFile(String filename)`: Read in the data from [triplog.csv](./triplog.csv) to the `trip` ArrayList. The idea is to initialize each line of data (Time,Latitude,Longitude) as a TripPoint object. You can then fill the `trip` ArrayList with those TripPoint objects. 

`totalTime()`: Should return the total time of the trip in hours. Remember the Time column in the data is in minutes. 

`haversineDistance(TripPoint a, TripPoint b)`: Should compute and return the Haversine distance between two points in kilometers. 

`totalDistance()`: Should compute and return the total distance of the trip in kilometers. Meaning the total distance between every point in the `trip` ArrayList. 

`avgSpeed(TripPoint a, TripPoint b)`: Should return the average speed between two points in kilometers per hour. This method should work no matter which order the points are given in. 

## New Methods (You should implement these for project 3)

`h1StopDetection()`: Should find and return the number of stops in the `trip` ArrayList. Use the first heuristic with a displacement threshold of 0.6 km. The method should also initialize and fill the `movingTrip` ArrayList with only the points that are moving (all the TripPoints minus the stops). Don't actually remove directly from `trip`, only `movingTrip`. This way we keep both the full trip and the moving trip.

`h2StopDetection()`: Should find and return the number of stops in the `trip` ArrayList. Use the second heuristic with a stop radius of 0.5 km. The method should also initialize and fill the `movingTrip` ArrayList with only the points that are moving (all the TripPoints minus the stops). Don't actually remove directly from `trip`, only `movingTrip`. This way we keep both the full trip and the moving trip.

`movingTime()`: Should find and return the amount of time spent moving in hours. 

`stoppedTime()`: Should find and return the amount of time spent stopped in hours. 

`avgMovingSpeed()`: Should find and return the average speed over the course of the trip only considering the moving points. Units should be km/hour. 

`getMovingTrip()`: Should return a copy of the `movingTrip` ArrayList.

## Grading

Plagiarism will not be tolerated under any circumstances. Participating students will be penalized depending on the degree of plagiarism. It includes "No-code" sharing among the students. It can lead to academic misconduct reporting to the authority if identical code is found among students. 

You will be graded on: 
* Zybooks submission: 80 points
* Github commits (10 commits): 10 points
* Javadocs: 10 points

Note that the data file used in Zybooks is different from the one in GitHub so your solution should be a general solution.

Submit your project before the due date/time. **No late submissions allowed.**
