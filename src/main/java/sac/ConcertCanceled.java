
package sac;

public class ConcertCanceled extends AbstractEvent {

    private Long id;

    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    private Long concertId;

}
