package mg.working.diakonacalendar.dto;


import mg.working.diakonacalendar.entity.ServiceSlot;

import java.util.UUID;

public record AssignGroupeRequest(UUID groupeId, ServiceSlot service) {}
