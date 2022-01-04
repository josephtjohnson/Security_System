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

    void createSensorList() {
        ArrayList<Sensor> arrlist = new ArrayList<Sensor>(3);
        arrlist.add(doorSensor);
        arrlist.add(windowSensor);
        arrlist.add(motionSensor);
    }


    void setAllSensorsToInactive() {
        securityService.changeSensorActivationStatus(doorSensor, false);
        securityService.changeSensorActivationStatus(windowSensor, false);
        securityService.changeSensorActivationStatus(motionSensor, false);
    }

    void setAllSensorsToActive() {
        securityService.changeSensorActivationStatus(doorSensor, true);
        securityService.changeSensorActivationStatus(windowSensor, true);
        securityService.changeSensorActivationStatus(motionSensor, true);
    }

    @BeforeEach
    void setUp() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    public void shouldBeInPendingAlarmStatus ()
    {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(doorSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    public void shouldSetPendingAlarmStatusToArmed () {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(windowSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void shouldReturnNoAlarmStatus () {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        setAllSensorsToInactive();
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

    }

    @Test
    public void shouldNotChangeAlarmState () {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        setAllSensorsToInactive();
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void sensorActiveSystemPendingChangeToAlarm () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        setAllSensorsToActive();
        securityService.changeSensorActivationStatus(doorSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void sensorDeactivatedWhileInactiveNoChangeToAlarmState () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        setAllSensorsToInactive();
        securityService.changeSensorActivationStatus(doorSensor, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    public void catIdentifiedWhileArmedHomeChangeSystemToAlarm () {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void catNotIdentifiedChangeStatusNoAlarmIfSensorsInactive () {
        setAllSensorsToInactive();
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void systemDisArmedSetStatusNoAlarm () {
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    public void systemArmedResetSensorsToInactive () {
        setAllSensorsToActive();
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        assertTrue(securityService.getSensors().stream().allMatch(sensor ->
                Boolean.FALSE.equals(sensor.getActive())));
    }

    @Test
    public void systemArmedHomeCatIdentifiedSetStatusAlarm () {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.processImage(mock(BufferedImage.class));
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

}

