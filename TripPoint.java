import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.IOException;

/**
 * Represents a geospatial tracking point with timestamp data.
 * Provides functionality for analyzing movement patterns and identifying stationary periods.
 */
public class TripPoint {
    // Geographic coordinates in decimal degrees
    private double lat;  // Latitude position (-90 to +90)
    private double lon;  // Longitude position (-180 to +180)
    
    // Temporal data in minutes since trip start
    private int time;    

    // Data collections for complete and filtered trip segments
    private static ArrayList<TripPoint> trip;       // Raw unfiltered trip data
    private static ArrayList<TripPoint> movingTrip; // Movement-only segments

    /**
     * Creates a default trip point at origin (0,0) with zero timestamp.
     */
    public TripPoint() {
        this(0, 0.0, 0.0);
    }

    /**
     * Creates a geospatial point with specific timing data.
     * @param time Minutes since trip commencement
     * @param lat Latitude coordinate in decimal degrees
     * @param lon Longitude coordinate in decimal degrees
     */
    public TripPoint(int time, double lat, double lon) {
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    // Accessor methods for point properties
    public int getTime() { return time; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }

    /**
     * Provides a defensive copy of the complete trip log.
     * @return New ArrayList containing all recorded points
     */
    public static ArrayList<TripPoint> getTrip() {
        return new ArrayList<>(trip);
    }

    /**
     * Provides a defensive copy of movement-only segments.
     * @return New ArrayList containing filtered moving points
     */
    public static ArrayList<TripPoint> getMovingTrip() {
        return new ArrayList<>(movingTrip);
    }

    /**
     * Computes great-circle distance between two geographic points.
     * Uses Haversine formula for spherical earth approximation.
     * @param first Initial geographic point
     * @param second Destination geographic point
     * @return Distance in kilometers between points
     */
    public static double haversineDistance(TripPoint first, TripPoint second) {
        // Convert degrees to radians for trigonometric functions
        double lat1 = Math.toRadians(first.getLat());
        double lat2 = Math.toRadians(second.getLat());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(second.getLon() - first.getLon());

        // Haversine formula components
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dLon / 2), 2);

        // Earth's mean radius in kilometers
        final double EARTH_RADIUS = 6371;
        return EARTH_RADIUS * 2 * Math.asin(Math.sqrt(a));
    }

    /**
     * Computes average travel speed between two recorded points.
     * @param a Starting point
     * @param b Ending point
     * @return Speed in kilometers per hour, or zero if time interval is zero
     */
    public static double avgSpeed(TripPoint a, TripPoint b) {
        int minutes = Math.abs(a.getTime() - b.getTime());
        double dist = haversineDistance(a, b);
        return (minutes == 0) ? 0.0 : (dist / minutes) * 60;
    }

    /**
     * Calculates total elapsed time for the complete journey.
     * @return Duration in hours from first to last recorded point
     */
    public static double totalTime() {
        return trip.isEmpty() ? 0 : trip.get(trip.size() - 1).getTime() / 60.0;
    }

    /**
     * Computes cumulative distance traveled across all segments.
     * @return Total journey distance in kilometers
     * @throws IOException If trip data cannot be loaded from default file
     */
    public static double totalDistance() throws IOException {
        double total = 0.0;
        if (trip.isEmpty()) readFile("triplog.csv");

        for (int i = 1; i < trip.size(); i++) {
            total += haversineDistance(trip.get(i - 1), trip.get(i));
        }
        return total;
    }

    /**
     * Identifies stationary periods using consecutive point analysis.
     * Points within 0.6km of previous point are considered stops.
     * @return Count of stationary intervals detected
     */
    public static int h1StopDetection() {
        movingTrip = new ArrayList<>();
        if (trip.isEmpty()) return 0;

        int stops = 0;
        final double STOP_THRESHOLD = 0.6; // Kilometers
        movingTrip.add(trip.get(0));

        for (int i = 1; i < trip.size(); i++) {
            double dist = haversineDistance(trip.get(i - 1), trip.get(i));
            if (dist > STOP_THRESHOLD) {
                movingTrip.add(trip.get(i));
            } else {
                stops++;
            }
        }
        return stops;
    }

    /**
     * Identifies stationary clusters using spatial density analysis.
     * Groups of 3+ points within 0.5km radius are considered stops.
     * @return Total number of points identified in stop clusters
     */
    public static int h2StopDetection() {
        movingTrip = new ArrayList<>();
        final double CLUSTER_RADIUS = 0.5; // Kilometers
        int stopPoints = 0;

        ArrayList<TripPoint> currentCluster = new ArrayList<>();
        for (TripPoint current : trip) {
            if (currentCluster.isEmpty()) {
                currentCluster.add(current);
                continue;
            }

            boolean inCluster = false;
            for (TripPoint clusterPoint : currentCluster) {
                if (haversineDistance(clusterPoint, current) < CLUSTER_RADIUS) {
                    inCluster = true;
                    break;
                }
            }

            if (inCluster) {
                currentCluster.add(current);
            } else {
                if (currentCluster.size() >= 3) {
                    stopPoints += currentCluster.size();
                } else {
                    movingTrip.addAll(currentCluster);
                }
                currentCluster.clear();
                currentCluster.add(current);
            }
        }
        
        // Process final cluster
        if (currentCluster.size() >= 3) {
            stopPoints += currentCluster.size();
        } else {
            movingTrip.addAll(currentCluster);
        }

        return stopPoints;
    }

    /**
     * Computes duration of active movement periods.
     * @return Total moving time in hours
     */
    public static double movingTime() {
        if (movingTrip == null || movingTrip.size() < 2) {
            return 0.0;
        }
        return (movingTrip.size() - 1) * 5 / 60.0; // 5 minutes between readings
    }

    /**
     * Computes duration of stationary periods.
     * @return Total stopped time in hours
     */
    public static double stoppedTime() {
        return totalTime() - movingTime();
    }

    /**
     * Calculates mean velocity during movement periods.
     * @return Average speed in km/h during active travel
     */
    public static double avgMovingSpeed() {
        if (movingTrip == null || movingTrip.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 1; i < movingTrip.size(); i++) {
            totalDistance += haversineDistance(movingTrip.get(i - 1), movingTrip.get(i));
        }
        
        double duration = movingTime();
        return (duration > 0) ? totalDistance / duration : 0.0;
    }

    /**
     * Loads trip data from specified CSV file.
     * Expected format: Time(min),Latitude,Longitude
     * @param filename Path to CSV data file
     * @throws IOException If file cannot be read
     */
    public static void readFile(String filename) throws IOException {
        trip = new ArrayList<>();
        
        try (Scanner fileScanner = new Scanner(new File(filename))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (!line.startsWith("Time")) {  // Skip header
                    String[] fields = line.split(",");
                    trip.add(new TripPoint(
                        Integer.parseInt(fields[0].trim()),
                        Double.parseDouble(fields[1].trim()),
                        Double.parseDouble(fields[2].trim())
                    ));
                }
            }
        }
    }

    /**
     * Generates formatted string representation of tracking point.
     * @return String in "Time: [min], Lat: [deg], Lon: [deg]" format
     */
    @Override
    public String toString() {
        return String.format("Time: %d, Lat: %.6f, Lon: %.6f", time, lat, lon);
    }
}