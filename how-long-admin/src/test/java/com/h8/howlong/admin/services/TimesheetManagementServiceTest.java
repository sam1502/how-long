package com.h8.howlong.admin.services;

import com.h8.howlong.domain.WorkDay;
import com.h8.howlong.services.TimesheetContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

class TimesheetManagementServiceTest {

    private TimesheetManagementService service;

    private TimesheetContextService contextService;

    private int month;
    private int day;

    @BeforeEach
    void setUp() {
        contextService = mock(TimesheetContextService.class);
        service = new TimesheetManagementService(contextService);

        month = 9;
        day = 23;
    }

    @Test
    void shouldCallUpdateWorkDayOnTimesheetContextServiceWithProperStartTimeArgument()
            throws TimesheetManagementFailedException {
        //given
        var startDateTimeBeforeUpdate = LocalDateTime.of(2018, month, day, 8, 0);
        var startDateTimeToUpdate = LocalDateTime.of(2018, month, day, 9, 15);
        var startTimeToUpdate = startDateTimeToUpdate.toLocalTime();
        var endDateTime = LocalDateTime.of(2018, month, day, 16, 45);

        var workday = WorkDay.builder()
                .start(startDateTimeBeforeUpdate)
                .end(endDateTime)
                .build();

        //when
        when(contextService.getWorkDayOf(month, day))
                .thenReturn(Optional.of(workday));

        service.updateStartTime(month, day, startTimeToUpdate);

        //then
        var workdayCaptor = ArgumentCaptor.forClass(WorkDay.class);
        verify(contextService, times(1)).updateWorkDay(workdayCaptor.capture());

        assertThat(workdayCaptor.getValue().getStart())
                .isEqualTo(startDateTimeToUpdate);
    }

    @Test
    void shouldCallUpdateWorkDayOnTimesheetContextServiceWithProperEndTimeArgument()
            throws TimesheetManagementFailedException {
        //given
        var startDateTime = LocalDateTime.of(2018, month, day, 7, 45);
        var endDateTimeBeforeUpdate = LocalDateTime.of(2018, month, day, 16, 0);
        var endDateTimeToUpdate = LocalDateTime.of(2018, month, day, 17, 15);
        var endTimeToUpdate = endDateTimeToUpdate.toLocalTime();

        var workday = WorkDay.builder()
                .start(startDateTime)
                .end(endDateTimeBeforeUpdate)
                .build();

        //when
        when(contextService.getWorkDayOf(month, day))
                .thenReturn(Optional.of(workday));

        service.updateEndTime(month, day, endTimeToUpdate);

        //then
        var workdayCaptor = ArgumentCaptor.forClass(WorkDay.class);
        verify(contextService, times(1)).updateWorkDay(workdayCaptor.capture());

        assertThat(workdayCaptor.getValue().getEnd())
                .isEqualTo(endDateTimeToUpdate);
    }

    @Test
    void shouldThrowATimeSheetManagementFailedExceptionWhenGivenStartTimeIsAfterActualEndTime() {
        var startDateTimeBeforeUpdate = LocalDateTime.of(2018, month, day, 8, 0);
        var startDateTimeToUpdate = LocalDateTime.of(2018, month, day, 20, 15);
        var startTimeToUpdate = startDateTimeToUpdate.toLocalTime();
        var endDateTime = LocalDateTime.of(2018, month, day, 16, 45);

        var workday = WorkDay.builder()
                .start(startDateTimeBeforeUpdate)
                .end(endDateTime)
                .build();

        //when
        when(contextService.getWorkDayOf(month, day))
                .thenReturn(Optional.of(workday));

        var thrown = catchThrowable(() -> service.updateStartTime(month, day, startTimeToUpdate));

        //then
        assertThat(thrown)
                .isInstanceOf(TimesheetManagementFailedException.class)
                .hasMessage("Provided start time '%s' is after end time '%s' of the given day",
                        startDateTimeToUpdate, workday.getEnd());
    }

    @Test
    void shouldThrowATimeSheetManagementFailedExceptionWhenGivenEndTimeIsBeforeActualStartTime() {
        //given
        var startDateTime = LocalDateTime.of(2018, month, day, 15, 45);
        var endDateTimeBeforeUpdate = LocalDateTime.of(2018, month, day, 16, 0);
        var endDateTimeToUpdate = LocalDateTime.of(2018, month, day, 14, 15);
        var endTimeToUpdate = endDateTimeToUpdate.toLocalTime();

        var workday = WorkDay.builder()
                .start(startDateTime)
                .end(endDateTimeBeforeUpdate)
                .build();

        //when
        when(contextService.getWorkDayOf(month, day))
                .thenReturn(Optional.of(workday));

        var thrown = catchThrowable(() -> service.updateEndTime(month, day, endTimeToUpdate));

        //then
        assertThat(thrown)
                .isInstanceOf(TimesheetManagementFailedException.class)
                .hasMessage("Provided end time '%s' is before start time '%s' of the given day",
                        endDateTimeToUpdate, workday.getStart());
    }

    @Test
    void shouldCallDeleteOnTimesheetContextServiceWithProperArguments()
            throws TimesheetManagementFailedException {
        //when
        when(contextService.deleteWorkday(anyInt(), anyInt()))
                .thenReturn(true);

        service.delete(month, day);

        //then
        var monthCaptor = ArgumentCaptor.forClass(Integer.class);
        var dayCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(contextService, times(1)).deleteWorkday(monthCaptor.capture(), dayCaptor.capture());

        assertThat(monthCaptor.getValue()).isEqualTo(month);
        assertThat(dayCaptor.getValue()).isEqualTo(day);
    }

    @Test
    void shouldThrowATimeSheetManagementFailedExceptionWhenWorkdayCouldNotBeDeleted() {
        //when
        when(contextService.deleteWorkday(anyInt(), anyInt()))
                .thenReturn(false);

        var thrown = catchThrowable(() -> service.delete(month, day));

        //then
        assertThat(thrown)
                .isInstanceOf(TimesheetManagementFailedException.class)
                .hasMessage("Provided day could not be deleted");
    }
}
