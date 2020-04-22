package sac;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

 @RestController
 public class TicketBookSystemController {

     @Autowired
     TicketBookSystemRepository ticketBookSystemRepository;

@RequestMapping(value = "/ticket",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8")
public void ticketBook(@RequestBody TicketBooked ticketBook)
        throws Exception {
        System.out.println("##### /ticketBookSystem/ticketBook  called #####");
        //@RequestParam Long customerId, @RequestParam String status, @RequestParam Long concertId
        //TicketBooked ticketBook


        TicketBookSystem  ticketBookSystem = new TicketBookSystem();
        ticketBookSystem.setConcertId(ticketBook.getConcertId());
        ticketBookSystem.setCustomerId(ticketBook.getCustomerId());
        ticketBookSystem.setStatus("예약완료");
        ticketBookSystemRepository.save(ticketBookSystem);

        TicketBooked ticketBooked = new TicketBooked();
        ticketBooked.setConcertId(ticketBook.getConcertId());
        ticketBooked.setCustomerId(ticketBook.getCustomerId());
        ticketBooked.setStatus("예약완료");
        ticketBooked.publish();


        }

@RequestMapping(value = "/cancel",
        method = RequestMethod.POST,
        produces = "application/json;charset=UTF-8")

public void ticketCancelation(@RequestBody TicketCancel ticketCancel)
        throws Exception {
        System.out.println("##### /ticketBookSystem/ticketCancelation  called #####");

        TicketBookSystem  ticketBookSystem = new TicketBookSystem();
        ticketBookSystem.setConcertId(ticketCancel.getConcertId());
        ticketBookSystem.setStatus("예약취소");
        ticketBookSystemRepository.save(ticketBookSystem);

       TicketCanceled ticketCanceled = new TicketCanceled();
       ticketCanceled.setConcertId(ticketCancel.getConcertId());
       ticketCanceled.publish();
        }
 }
