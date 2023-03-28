package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ParkingLotRepository;
import com.driver.repository.ReservationRepository;
import com.driver.repository.SpotRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    UserRepository userRepository3;
    @Autowired
    SpotRepository spotRepository3;
    @Autowired
    ReservationRepository reservationRepository3;
    @Autowired
    ParkingLotRepository parkingLotRepository3;
    @Override
    public Reservation reserveSpot(Integer userId, Integer parkingLotId, Integer timeInHours, Integer numberOfWheels) throws Exception {
        try{
            if(!userRepository3.findById(userId).isPresent() || !parkingLotRepository3.findById(parkingLotId).isPresent()){
                throw new Exception("Cannot make reservation");
            }

            User user=userRepository3.findById(userId).get();
            ParkingLot parkingLot=parkingLotRepository3.findById(parkingLotId).get();

            List<Spot>spotList=parkingLot.getSpotList();
            boolean isSpotPresent=false;
            for(Spot spot:spotList){
                if(!spot.getOccupied()){
                    isSpotPresent=true;
                    break;
                }
            }

            if(!isSpotPresent){
                throw new Exception("Cannot make reservation");
            }

            SpotType spotType;

            if(numberOfWheels>4){
                spotType=SpotType.OTHERS;
            }
            else if (numberOfWheels>2) {
                spotType=SpotType.FOUR_WHEELER;
            }
            else{
                spotType=SpotType.TWO_WHEELER;
            }

            isSpotPresent=false;

            Spot desiredSpot=null;

            int min=Integer.MAX_VALUE;

            for(Spot spot:spotList){
                if(spotType.equals(SpotType.OTHERS) && spot.getSpotType().equals(SpotType.OTHERS)){
                    if(spot.getPricePerHour()*timeInHours <min && !spot.getOccupied()){
                        min=spot.getPricePerHour()*timeInHours;
                        isSpotPresent=true;
                        desiredSpot=spot;
                    }
                }
                else if(spotType.equals(SpotType.FOUR_WHEELER) && spot.getSpotType().equals(SpotType.OTHERS) ||
                        spot.getSpotType().equals(SpotType.FOUR_WHEELER)){
                    if(spot.getPricePerHour()*timeInHours <min && !spot.getOccupied()){
                        min=spot.getPricePerHour()*timeInHours;
                        isSpotPresent=true;
                        desiredSpot=spot;
                    }
                }
                else if(spotType.equals(SpotType.TWO_WHEELER) && spot.getSpotType().equals(SpotType.OTHERS)
                || spot.getSpotType().equals(SpotType.FOUR_WHEELER) || spot.getSpotType().equals(SpotType.TWO_WHEELER)){
                    if(spot.getPricePerHour()*timeInHours <min && !spot.getOccupied()){
                        min=spot.getPricePerHour()*timeInHours;
                        isSpotPresent=true;
                        desiredSpot=spot;
                    }
                }
            }

            if(!isSpotPresent){
                throw new Exception("Cannot make reservation");
            }

            assert desiredSpot!=null;
            desiredSpot.setOccupied(true);


            Reservation reservation=new Reservation();
            reservation.setNumberOfHours(timeInHours);
            reservation.setSpot(desiredSpot);
            reservation.setUser(user);

            desiredSpot.getReservationList().add(reservation);
            user.getReservationList().add(reservation);

            userRepository3.save(user);
            spotRepository3.save(desiredSpot);

            return reservation;
        }
        catch (Exception e){
            return null;
        }
    }
}
