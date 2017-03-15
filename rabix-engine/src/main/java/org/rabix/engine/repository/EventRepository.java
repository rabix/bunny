package org.rabix.engine.repository;

import java.util.List;
import java.util.UUID;

import org.rabix.engine.event.Event;
import org.rabix.engine.event.Event.EventStatus;
import org.rabix.engine.event.Event.PersistentEventType;

public interface EventRepository {

  void insert(UUID id, PersistentEventType type, Event event, EventStatus status);
  
  void update(UUID id, PersistentEventType type, EventStatus status);
  
  void delete(UUID id);
  
  List<Event> findUnprocessed();
  
  public class EventEntity {
    
    PersistentEventType type;
    Event event;
    EventStatus status;
    
    public EventEntity(PersistentEventType type, Event event, EventStatus status) {
      super();
      this.type = type;
      this.event = event;
      this.status = status;
    }

    public PersistentEventType getType() {
      return type;
    }
    
    public void setType(PersistentEventType type) {
      this.type = type;
    }
    
    public Event getEvent() {
      return event;
    }
    
    public void setEvent(Event event) {
      this.event = event;
    }
    
    public EventStatus getStatus() {
      return status;
    }
    
    public void setStatus(EventStatus status) {
      this.status = status;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((event == null) ? 0 : event.hashCode());
      result = prime * result + ((status == null) ? 0 : status.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      EventEntity other = (EventEntity) obj;
      if (event == null) {
        if (other.event != null)
          return false;
      } else if (!event.equals(other.event))
        return false;
      if (status != other.status)
        return false;
      if (type != other.type)
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "EventEntity [type=" + type + ", event=" + event + ", status=" + status + "]";
    }
  }
  
}
