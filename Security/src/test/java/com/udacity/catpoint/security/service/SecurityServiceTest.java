package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageServiceInterface;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest
{
    @Mock
    public SecurityService securityService;

    @Mock
    public ImageServiceInterface imageService;

    @Mock
    public SecurityRepository securityRepository;

    static HashSet<Sensor> sensorCollection = new HashSet<>();

    Sensor testSensor = new Sensor("DOOR", SensorType.DOOR);

    static void createSensorSet() {
        Sensor doorSensor = new Sensor("DOOR", SensorType.DOOR);
        Sensor windowSensor = new Sensor("WINDOW", SensorType.WINDOW);
        Sensor motionSensor = new Sensor("MOTION", SensorType.MOTION);
        sensorCollection.add(doorSensor);
        sensorCollection.add(windowSensor);
        sensorCollection.add(motionSensor);
    }


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
        testSensor.setActive(true);
        securityService.changeSensorActivationStatus(testSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
    //Test 2
    @Test
    public void shouldSetPendingAlarmStatusToArmed () {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        testSensor.setActive(false);
        securityService.changeSensorActivationStatus(testSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    //Test 3
    @Test
    public void shouldReturnNoAlarmStatus () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        testSensor.setActive(false);
        securityService.changeSensorActivationStatus(testSensor, true);
        securityService.changeSensorActivationStatus(testSensor, false);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    //Test 4
    @Test
    public void shouldNotChangeAlarmState () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        testSensor.setActive(false);
        securityService.changeSensorActivationStatus(testSensor, true);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
        verify(securityRepository, never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
    //Test 5
    @Test
    public void sensorActiveSystemPendingChangeToAlarm () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        testSensor.setActive(true);
        securityService.changeSensorActivationStatus(testSensor, true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
    //Test 6
    @Test
    public void sensorDeactivatedWhileInactiveNoChangeToAlarmState () {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        testSensor.setActive(false);
        securityService.changeSensorActivationStatus(testSensor, false);
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
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        testSensor.setActive(false);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    //Test 9
    @Test
    public void systemDisArmedSetStatusNoAlarm () {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }
    //Test 10
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    public void systemArmedResetSensorsToInactive (ArmingStatus status) {
        createSensorSet();
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(sensorCollection);
        sensorCollection.forEach(sensor -> sensor.setActive(true));
        securityService.setArmingStatus(status);
        sensorCollection.forEach(sensor -> Assertions.assertEquals(false, sensor.getActive()));
    }

    //Test 11
    @Test
    public void systemArmedHomeCatIdentifiedSetStatusAlarm () {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //Test 12
    @Test
    public void systemArmedSetStatusAlarm () {
        when(securityService.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));
        securityService.setArmingStatus(securityService.getArmingStatus());
        verify(securityRepository, times(2)).setAlarmStatus(AlarmStatus.ALARM);
    }
}

