package com.rental.rentalonline.service;

import com.rental.rentalonline.exception.StayDeleteException;
import com.rental.rentalonline.exception.StayNotExistException;
import com.rental.rentalonline.model.*;
import com.rental.rentalonline.repository.LocationRepository;
import com.rental.rentalonline.repository.ReservationRepository;
import com.rental.rentalonline.repository.StayRepository;
import com.rental.rentalonline.repository.StayReservationDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class StayService {
    private StayRepository stayRepository;
    private LocationRepository locationRepository;
    private ReservationRepository reservationRepository;
    private ImageStorageService imageStorageService;
    private GeoCodingService geoCodingService;
    private StayReservationDateRepository stayReservationDateRepository;

    @Autowired
    public StayService(StayRepository stayRepository, LocationRepository locationRepository, ReservationRepository reservationRepository, ImageStorageService imageStorageService, GeoCodingService geoCodingService, StayReservationDateRepository stayReservationDateRepository) {
        this.stayRepository = stayRepository;
        this.locationRepository = locationRepository;
        this.reservationRepository = reservationRepository;
        this.imageStorageService = imageStorageService;
        this.geoCodingService = geoCodingService;
        this.stayReservationDateRepository = stayReservationDateRepository;
    }


    public List<Stay> listByUser(String username) {
        return stayRepository.findByHost(new User.Builder().setUsername(username).build());
    }

    public Stay findByIdAndHost(Long stayId, String username) throws StayNotExistException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }
        return stay;
    }
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void add(Stay stay, MultipartFile[] images) {
        List<String> mediaLinks = Arrays.stream(images).parallel().map(image -> imageStorageService.save(image)).collect(Collectors.toList());
        List<StayImage> stayImages=new ArrayList<>();
        for(String url: mediaLinks){
            stayImages.add(new StayImage(url,stay));
        }
        stay.setImages(stayImages);
        stayRepository.save(stay);

        Location location = geoCodingService.getLatLng(stay.getId(), stay.getAddress());
        locationRepository.save(location);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void delete(Long stayId, String username) throws StayNotExistException {
        Stay stay = stayRepository.findByIdAndHost(stayId, new User.Builder().setUsername(username).build());
        if (stay == null) {
            throw new StayNotExistException("Stay doesn't exist");
        }
        List<Reservation> reservations = reservationRepository.findByStayAndCheckoutDateAfter(stay, LocalDate.now());
        if (reservations != null && reservations.size() > 0) {
            throw new StayDeleteException("Cannot delete stay with active reservation");
        }

        stayRepository.deleteById(stayId);
    }
}