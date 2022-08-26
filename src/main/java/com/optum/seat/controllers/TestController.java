package com.optum.seat.controllers;

import java.security.Principal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.optum.seat.models.AllBookingTransaction;
import com.optum.seat.models.Location;
import com.optum.seat.models.User;
import com.optum.seat.models.UserLastBooking;
import com.optum.seat.repository.LocationRepo;
import com.optum.seat.repository.UserLastBookingRepository;
import com.optum.seat.repository.UserRepository;
import com.optum.seat.services.AllBookingTransactionService;
import com.optum.seat.services.LocationService;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
	 @Autowired
	 LocationService locationService;
	 @Autowired
	 UserRepository userrepo;
	 @Autowired
	 AllBookingTransactionService allBookingTransactionService;
	 @Autowired
	 LocationRepo locationRepo;
	 @Autowired
	 UserLastBookingRepository userLastBookingRepository;
	 
	 @GetMapping("/all")
	  public String allAccess() {
	    return "Office Seat Booking Portal.";
	  }
	  @GetMapping("/user")
	  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	  public String userAccess() {
	    return "User Content.";
	  }
	  @GetMapping("/user/profile")
	  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	  public Optional<User> profile(Principal principal) {
		  String name= principal.getName();
		  return userrepo.findByEmpID(name);
	  }
	  
	  @PostMapping("/user/book/{Code}")
	  @Transactional
	  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	  public String BookSeatAtLocationCode(@PathVariable("Code") String code, Principal principal,
				@RequestBody AllBookingTransaction allBookingTransaction) {
			   //Take JSON in format {
	       // "dateOfTransaction" : "2022-08-24",
	       // "slotTime" : "II"}
			allBookingTransaction.setEmpID(principal.getName());
			List <UserLastBooking> listlastbooking = userLastBookingRepository.findByEmpID(principal.getName());
			if (listlastbooking.size()!=0) {
				if (listlastbooking.get(0).getDate().toString().equals(allBookingTransaction.getDateOfTransaction().toString())
					&& listlastbooking.get(0).getSlot().equals(allBookingTransaction.getSlotTime())) {
				return "You have already booked for this slot";
			}
				if (listlastbooking.get(0).getDate().toString().equals(allBookingTransaction.getDateOfTransaction().toString())
						&& !(listlastbooking.get(0).getSlot().equals(allBookingTransaction.getSlotTime()))) {
					return "You have already booked for another slot on same day. Please cancel previous booking to book this slot.";
				}
			}
			Integer trx = allBookingTransactionService.bookSeat(code, allBookingTransaction);
			UserLastBooking userlastbooking = null;
			if (listlastbooking.size()==0) {
				 userlastbooking = new UserLastBooking(principal.getName(), allBookingTransaction.getSlotTime(), 
						allBookingTransaction.getDateOfTransaction(), trx);	
			}else {
				userlastbooking = listlastbooking.get(0);
				userlastbooking.setDate(allBookingTransaction.getDateOfTransaction());
				userlastbooking.setLast_trxn_id(trx);
				userlastbooking.setSlot(allBookingTransaction.getSlotTime());
			}
			
			userLastBookingRepository.save(userlastbooking);
			return "success";
		
		}
	  @PostMapping("/user/cancel")
	  @Transactional
	  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	  public String CancelSeat(Principal principal) {
		    String empID = principal.getName();
		    List <UserLastBooking> list = userLastBookingRepository.findByEmpID(empID);
		    Integer trxID = list.get(0).getLast_trxn_id();
		    if (!allBookingTransactionService.isTransactionPresent(trxID)) {
		    	return "No Cancellation available";
		    }
		    
			allBookingTransactionService.DeletebyId(trxID);
			userLastBookingRepository.deleteById(list.get(0).getId());
			return "success";
		}
	  
	  @GetMapping("/user/seatleftbydateslot/{code}")
	  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	  public Integer SeatLeftByDateSlot(@PathVariable("code")String code,@RequestBody AllBookingTransaction allBookingTransaction)
	    {
	    	Date date=allBookingTransaction.getDateOfTransaction();
	    	String slotTimeString=allBookingTransaction.getSlotTime();
	    //	Integer n= locationRepo.
	    	Integer countbooking =   allBookingTransactionService.CountBookingByDateAndSlot(code, date, slotTimeString);
	    	Integer seatLimit = locationRepo.findByLocationCode(code).get(0).getLocationSeatLimit();
	    	return seatLimit - countbooking;
	    }
	  
	  @GetMapping("/user/availableoffice/{place}")
	  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	  public List<String> availableoffice(@PathVariable("place") String place, @RequestParam(name="date",required = true, defaultValue = "1990-01-01") Date date, 
			  @RequestParam(name="slot",required = true, defaultValue = "AM") String slot){
		  List <Location> codes = locationRepo.findByLocationName(place);
		  List <String> ans = new ArrayList<>();
		  for (var loc : codes) {
			  if (loc.getLocationSeatLimit()>allBookingTransactionService.CountBookingByDateAndSlot(loc.getLocationCode(), date, slot)) {
				  ans.add(loc.getLocationCode());
			  }
		  }
		  return ans;
		  
	  }
	  
	  
	  @GetMapping("/admin")
	  @PreAuthorize("hasRole('ADMIN')")
	  public String adminAccess() {
	    return "Admin Board.";
	  }
	  @PostMapping("/admin/location/create")
	  @PreAuthorize("hasRole('ADMIN')")

	  public String createLocation(@RequestBody Location location){
			locationService.createLocation(location);
			return "success";
		}
	  //list of all locations
	  @GetMapping("/admin/location/list")
	  @PreAuthorize("hasRole('ADMIN')")
	  public List<Location> getListLocation()
		{ 
			return locationService.listLocation();
		}
	  
	  @PostMapping ("/admin/location/update/byCode")
	  @PreAuthorize("hasRole('ADMIN')")
	  public String updateLocationByCode(@RequestParam(name="code",required = false, defaultValue = "*****") String code,@RequestBody Location location)
		{
			boolean ret=locationService.editByLocationCode(code, location);
			return ret==true?"Success" :"Not a valid code";
		}
	  
	  @GetMapping ("/admin/getbookingpertimeslot/{code}")
	  @PreAuthorize("hasRole('ADMIN')")
	  public List<AllBookingTransaction> getbookingpertimeslot(@RequestParam(name="date",required = true, defaultValue = "*****") Date date,
			  @RequestParam(name="slot",required = true, defaultValue = "*****") String slot, @PathVariable("code") String code)
	  {
		  return allBookingTransactionService.getbookingpertimeslotservice(date, slot, code);
	  }
}
