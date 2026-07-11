import java.util.*;

// 1. Enums
enum VehicleType { CAR, BIKE, TRUCK }
enum SpotType { COMPACT, LARGE, MOTORCYCLE }
enum PaymentStatus { PENDING, COMPLETED, FAILED }

// 2. Interfaces & Strategies
interface PaymentService {
    boolean processPayment(double amount);
}

interface VehicleAllocationStrategy {
    List<SpotType> getAllowedSpotTypes(VehicleType vehicleType);
}

class ConfigBasedVehicleAllocationStrategy implements VehicleAllocationStrategy {
    private final Map<VehicleType, List<SpotType>> vehicleToSpotMapping;

    public ConfigBasedVehicleAllocationStrategy(Map<VehicleType, List<SpotType>> vehicleToSpotMapping) {
        this.vehicleToSpotMapping = vehicleToSpotMapping;
    }

    @Override
    public List<SpotType> getAllowedSpotTypes(VehicleType vehicleType) {
        return vehicleToSpotMapping.getOrDefault(vehicleType, Collections.emptyList());
    }
}

// 3. Domain Entities
class Vehicle {
    private final VehicleType vehicleType;
    private final String vehicleNumber;

    public Vehicle(VehicleType vehicleType, String vehicleNumber) {
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
    }

    public VehicleType getVehicleType() { return vehicleType; }
    public String getVehicleNumber() { return vehicleNumber; }
}

class Spot {
    private final SpotType spotType;
    private final int spotNumber;
    private boolean isOccupied;
    private Vehicle currentVehicle;
    private final int floorNumber;

    public Spot(SpotType spotType, int spotNumber, int floorNumber) {
        this.spotType = spotType;
        this.spotNumber = spotNumber;
        this.floorNumber = floorNumber;
        this.isOccupied = false;
        this.currentVehicle = null;
    }

    public SpotType getSpotType() { return spotType; }
    public int getSpotNumber() { return spotNumber; }
    public boolean isOccupied() { return isOccupied; }
    public int getFloorNumber() { return floorNumber; }

    public boolean occupySpot(Vehicle vehicle) {
        if (isOccupied) return false;
        this.currentVehicle = vehicle;
        this.isOccupied = true;
        return true;
    }

    public boolean freeSpot() {
        if (!isOccupied) return false;
        this.currentVehicle = null;
        this.isOccupied = false;
        return true;
    }
}

class Ticket {
    private final String ticketNumber;
    private final Vehicle vehicle;
    private final Spot spot;
    private final long entryTime;
    private long exitTime;
    private double parkingFee;
    private PaymentStatus paymentStatus;

    public Ticket(String ticketNumber, Vehicle vehicle, Spot spot) {
        this.ticketNumber = ticketNumber;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public String getTicketNumber() { return ticketNumber; }
    public Vehicle getVehicle() { return vehicle; }
    public Spot getSpot() { return spot; }
    public long getEntryTime() { return entryTime; }
    public long getExitTime() { return exitTime; }
    public void setExitTime(long exitTime) { this.exitTime = exitTime; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setParkingFee(double parkingFee) { this.parkingFee = parkingFee; }
}

class ParkingFloor {
    private final int floorNumber;
    private final List<Spot> spots;

    public ParkingFloor(int floorNumber, Map<SpotType, Integer> spotTypeToCount) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>();
        int spotCounter = 1;
        for (Map.Entry<SpotType, Integer> entry : spotTypeToCount.entrySet()) {
            SpotType spotType = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                spots.add(new Spot(spotType, spotCounter++, this.floorNumber));
            }
        }
    }

    public int getFloorNumber() { return floorNumber; }
    public List<Spot> getSpots() { return spots; }


    // ----------- public API for ParkingFloor -----------
    public Spot findAvailableSpot(List<SpotType> allowedSpotTypes) {
        for (Spot spot : spots) {
            if (!spot.isOccupied() && allowedSpotTypes.contains(spot.getSpotType())) {
                return spot;
            }
        }
        return null;
    }

    public boolean occupySpot(Spot spot, Vehicle vehicle) {
        return spot.occupySpot(vehicle);
    }

    public boolean freeSpot(Spot spot) {
        return spot.freeSpot();
    }
}

class ParkingSpotManager {
    private final List<ParkingFloor> parkingFloorList;
    private final VehicleAllocationStrategy vehicleAllocationStrategy;

    public ParkingSpotManager(List<ParkingFloor> parkingFloorList, VehicleAllocationStrategy vehicleAllocationStrategy) {
        this.parkingFloorList = parkingFloorList;
        this.vehicleAllocationStrategy = vehicleAllocationStrategy;
    }

    private ParkingFloor getFloorForSpot(Spot spot) {
        return parkingFloorList.stream()
                .filter(floor -> floor.getFloorNumber() == spot.getFloorNumber())
                .findFirst()
                .orElse(null);
    }

    private Spot findAvailableSpot(Vehicle vehicle) {
        List<SpotType> allowedSpotTypes = vehicleAllocationStrategy.getAllowedSpotTypes(vehicle.getVehicleType());
        for (ParkingFloor floor : parkingFloorList) {
            Spot availableSpot = floor.findAvailableSpot(allowedSpotTypes);
            if (availableSpot != null) {
                return availableSpot;
            }
        }
        return null;
    }

    private boolean occupySpot(Spot spot, Vehicle vehicle) {
        ParkingFloor floor = getFloorForSpot(spot);
        if (floor == null) return false;
        return floor.occupySpot(spot, vehicle);
    }


    // ---------- public API for ParkingSpotManager ----------
    // better public API , keep the checking for available spot and occupying in single atomic operation, better for concurrency
    public Spot assignSpot(Vehicle vehicle) {
        Spot availableSpot = findAvailableSpot(vehicle);
        if (availableSpot != null) {
            occupySpot(availableSpot, vehicle);
            return availableSpot;
        }
        return null;
    }

    public boolean freeSpot(Spot spot) {
        ParkingFloor floor = getFloorForSpot(spot);
        if (floor == null) return false;
        return floor.freeSpot(spot);
    }
}

class FeeCalculator {
    private final Map<VehicleType, Double> ratePerHour;

    public FeeCalculator(Map<VehicleType, Double> ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    public double calculateFee(Ticket ticket) {
        long duration = ticket.getExitTime() - ticket.getEntryTime();
        VehicleType vehicleType = ticket.getVehicle().getVehicleType();
        double rate = ratePerHour.getOrDefault(vehicleType, 0.0);

        // Fixed Integer division bug using Math.ceil to round up to the nearest hour
        double hours = Math.ceil((double) duration / (1000 * 60 * 60));
        return Math.max(1, hours) * rate; // Minimum 1-hour charge
    }
}

// 4. Main Controller
class ParkingLot {
    private final ParkingSpotManager parkingSpotManager;
    private final FeeCalculator feeCalculator;
    private final PaymentService paymentService;

    public ParkingLot(List<ParkingFloor> parkingFloors, Map<VehicleType, Double> ratePerHour,
                      VehicleAllocationStrategy vehicleAllocationStrategy, PaymentService paymentService) {
        this.parkingSpotManager = new ParkingSpotManager(parkingFloors, vehicleAllocationStrategy);
        this.feeCalculator = new FeeCalculator(ratePerHour);
        this.paymentService = paymentService;
    }

    public Ticket getParkingTicket(Vehicle vehicle) {
        Spot assignedSpot = parkingSpotManager.assignSpot(vehicle);
        if (assignedSpot == null) {
            System.out.println("No available spot for vehicle type: " + vehicle.getVehicleType());
            return null;
        }
        String ticketNumber = UUID.randomUUID().toString();
        return new Ticket(ticketNumber, vehicle, assignedSpot);
    }

    public boolean exitParkingLot(Ticket ticket) {
        ticket.setExitTime(System.currentTimeMillis());
        double fee = feeCalculator.calculateFee(ticket);
        ticket.setParkingFee(fee);

        boolean paymentSuccess = paymentService.processPayment(fee);
        if (paymentSuccess) {
            ticket.setPaymentStatus(PaymentStatus.COMPLETED);
            parkingSpotManager.freeSpot(ticket.getSpot());
            System.out.println("Payment successful. Goodbye!");
            return true;
        } else {
            ticket.setPaymentStatus(PaymentStatus.FAILED);
            System.out.println("Payment failed. Vehicle cannot exit.");
            return false;
        }
    }
}

class CardPaymentService implements PaymentService {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("Processing payment of $" + amount + " via Card.");
        return true;
    }
}

// 5. App Executor
public class ParkingLotApp {
    public static void main(String[] args) {
        System.out.println("Welcome to the Parking Lot System");

        Map<SpotType, Integer> floorConfig = new HashMap<>();
        floorConfig.put(SpotType.COMPACT, 10);
        floorConfig.put(SpotType.LARGE, 5);

        List<ParkingFloor> parkingFloorList = new ArrayList<>();
        parkingFloorList.add(new ParkingFloor(1, floorConfig));

        VehicleAllocationStrategy vehicleAllocationStrategy = new ConfigBasedVehicleAllocationStrategy(Map.of(
                VehicleType.CAR, List.of(SpotType.COMPACT, SpotType.LARGE),
                VehicleType.BIKE, List.of(SpotType.MOTORCYCLE),
                VehicleType.TRUCK, List.of(SpotType.LARGE)
        ));

        Map<VehicleType, Double> ratePerHour = Map.of(
                VehicleType.CAR, 10.0,
                VehicleType.BIKE, 5.0,
                VehicleType.TRUCK, 15.0
        );

        ParkingLot parkingLot = new ParkingLot(parkingFloorList, ratePerHour, vehicleAllocationStrategy, new CardPaymentService());

        // Simulating entry
        Vehicle car = new Vehicle(VehicleType.CAR, "MH-12-AB-1234");
        Ticket ticket = parkingLot.getParkingTicket(car);

        if (ticket != null) {
            System.out.println("Ticket Issued: " + ticket.getTicketNumber() + " at Spot: " + ticket.getSpot().getSpotNumber());
            // Simulating exit
            parkingLot.exitParkingLot(ticket);
        }
    }
}