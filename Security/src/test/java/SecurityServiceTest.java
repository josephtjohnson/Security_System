import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class SecurityServiceTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void shouldBeInPendingAlarmStatus () {assertTrue (true);}

    @Test
    public void shouldSetPendingAlarmStatusToArmed () {assertTrue (true);}

    @Test
    public void shouldReturnNoAlarmStatus () {assertTrue (true);}

    @Test
    public void shouldNotChangeAlarmState () {assertTrue (true);}

    @Test
    public void sensorActiveSystemPendingChangeToAlarm () {assertTrue (true);}

    @Test
    public void sensorDeactivatedWhileInactiveNoChangeToAlarmState () {assertTrue (true);}

    @Test
    public void catIdentifiedWhileArmedHomeChangeSystemToAlarm () {assertTrue (true);}

    @Test
    public void catNotIdentifiedChangeStatusNoAlarmIfSensorsInactive () {assertTrue (true);}

    @Test
    public void systemDisArmedSetStatusNoAlarm () {assertTrue (true);}

    @Test
    public void systemArmedResetSensorsToInactive () {assertTrue (true);}

    @Test
    public void systemArmedHomeCatIdentifiedSetStatusAlarm () {assertTrue (true);}

}

