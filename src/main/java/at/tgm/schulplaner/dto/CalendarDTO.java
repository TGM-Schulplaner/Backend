package at.tgm.schulplaner.dto;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * @author Georg Burkl
 * @version 2020-05-08
 */
@RequiredArgsConstructor
public class CalendarDTO {
    private final UUID id;
    private final UUID owner;
    private final String name;
}
