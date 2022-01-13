package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageServiceInterface;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

/**
 * Unit test for simple App.
 */
@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest
{
    /**
     * Rigorous Test :-)
     */
    @Mock
    private SecurityService securityService;

    @Mock
    private ImageServiceInterface imageService;

    @Mock
    private StatusListener statusListener;

    @Mock
    private SecurityRepository securityRepository;

    Sensor doorSensor = new Sensor("DOOR", SensorType.DOOR);
    Sensor windowSensor = new Sensor("WINDOW", SensorType.WINDOW);
    Sensor motionSensor = new Sensor("WINDOW", SensorType.MOTION);

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }
    //Test 1
    @Test
    public void shouldBeInPendingAlarmStatus ()
    {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(doorSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
    //Test 2
    @Test
    public void shouldSetPendingAlarmStatusToArmed () {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(windowSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    //Test 3
    @Test
    public void shouldReturnNoAlarmStatus () {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        doorSensor.setActive(false);
        securityService.changeSensorActivationStatus(doorSensor, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    //Test 4
    @Test
    public void shouldNotChangeAlarmState () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        doorSensor.setActive(false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    //Test 5
    @Test
    public void sensorActiveSystemPendingChangeToAlarm () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        doorSensor.setActive(true);
        securityService.changeSensorActivationStatus(doorSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    //Test 6
    @Test
    public void sensorDeactivatedWhileInactiveNoChangeToAlarmState () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        doorSensor.setActive(Boolean.FALSE);
        securityService.changeSensorActivationStatus(doorSensor, false);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.ALARM);
    }
    //Test 7
    @Test
    public void catIdentifiedWhileArmedHomeChangeSystemToAlarm () {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    //Test 8
    @Test
    public void catNotIdentifiedChangeStatusNoAlarmIfSensorsInactive () {
        doorSensor.setActive(false);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    //Test 9
    @Test
    public void systemDisArmedSetStatusNoAlarm () {
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    //Test 10
    @Test
    public void systemArmedResetSensorsToInactive () {
        doorSensor.setActive(true);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        assertTrue(securityService.getSensors().stream().allMatch(sensor ->
                Boolean.FALSE.equals(sensor.getActive())));
    }
    //Test 11
    @Test
    public void systemArmedHomeCatIdentifiedSetStatusAlarm () {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.processImage(mock(BufferedImage.class));
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

}

