package ride.example.demo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.example.ride.ws.CreateRideRequest;
import com.example.ride.ws.CreateRideResponse;
import com.example.ride.ws.GetAllRidesRequest;
import com.example.ride.ws.GetAllRidesResponse;
import com.example.ride.ws.RideInfo;

import ride.example.demo.model.Ride;
import ride.example.demo.repository.RideRepository;

@Endpoint
public class RideEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/ride/ws";
   @Autowired
    private  RideRepository rideRepository;

    public RideEndpoint(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateRideRequest")
    @ResponsePayload
    public CreateRideResponse createRide(@RequestPayload CreateRideRequest request) {
        Ride ride = new Ride();
        ride.setDeparture(request.getDeparture());
        ride.setDestination(request.getDestination());
        ride.setDepartureTime(DateUtils.toLocalDateTime(request.getDepartureTime()));
        ride.setAvailableSeats(request.getAvailableSeats());
        ride.setPrice(request.getPrice());

        rideRepository.save(ride);

        CreateRideResponse response = new CreateRideResponse();
        response.setRideId(ride.getId());
        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAllRidesRequest")
    @ResponsePayload
    public GetAllRidesResponse getAllRides(@RequestPayload GetAllRidesRequest request) {
        List<Ride> rides = rideRepository.findAll();

        GetAllRidesResponse response = new GetAllRidesResponse();
        for (Ride ride : rides) {
            RideInfo info = new RideInfo();
            info.setId(ride.getId());
            info.setDeparture(ride.getDeparture());
            info.setDestination(ride.getDestination());

            // Conversion LocalDateTime -> XMLGregorianCalendar
            info.setDepartureTime(DateUtils.toXMLGregorianCalendar(ride.getDepartureTime()));

            info.setAvailableSeats(ride.getAvailableSeats());
            info.setPrice(ride.getPrice());
            response.getRides().add(info);
        }

        return response;
    }
}
