package sac;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="TicketBookSystem_table")
public class TicketBookSystem {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long customerId;
    private String status;
    private Long concertId;

    /*
    @PostPersist
    public void onPostPersist(){
        TicketBooked ticketBooked = new TicketBooked();
        BeanUtils.copyProperties(this, ticketBooked);
        ticketBooked.publish();


    }

    @PostRemove
    public void onPostRemove(){
        TicketCanceled ticketCanceled = new TicketCanceled();
        BeanUtils.copyProperties(this, ticketCanceled);
        ticketCanceled.publish();


    }
*/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }




}
