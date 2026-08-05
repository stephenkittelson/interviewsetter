package org.kittelson.interviewsetter.appointments.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kittelson.interviewsetter.appointments.Appointment;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Instrumented UI test for {@link AppointmentAdapter}: it's what actually puts each appointment
 * on screen in the "Appts to Set" / "Appts to Confirm" list, including the red duplicate-flag
 * highlighting - so it's worth covering directly rather than only exercising it indirectly
 * through MainActivity (which additionally requires Google Sign-In and isn't practical to drive
 * from an instrumented test).
 */
@RunWith(AndroidJUnit4.class)
public class AppointmentAdapterTest {

    private Context context;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void normalAppointment_rendersCompanionsAndType_noHighlight() {
        Appointment appointment = new Appointment()
                .setTime(LocalDateTime.now())
                .setPresidencyMember("Smith")
                .setCompanions("Doe, John / Curie, Sam")
                .setLocation("the Bishop's office")
                .setAppointmentType("Ministering")
                .setStage("Set");

        AppointmentViewHolder holder = bindSingleAppointment(appointment);

        assertEquals("John Doe / Sam Curie (Ministering)", holder.textView.getText().toString());
        assertEquals("a non-duplicate appointment shouldn't be highlighted",
                android.graphics.Color.WHITE, backgroundColor(holder));
    }

    @Test
    public void duplicateAppointment_highlightedRed() {
        Appointment appointment = new Appointment()
                .setTime(LocalDateTime.now())
                .setPresidencyMember("Smith")
                .setCompanions("Doe, John")
                .setLocation("the Bishop's office")
                .setAppointmentType("Ministering")
                .setStage("Set");
        appointment.setDuplicate(true);

        AppointmentViewHolder holder = bindSingleAppointment(appointment);

        assertEquals("a duplicate appointment should be highlighted red",
                android.graphics.Color.RED, backgroundColor(holder));
    }

    @Test
    public void getItemCount_matchesAppointmentListSize() {
        List<Appointment> appointments = Arrays.asList(
                new Appointment().setTime(LocalDateTime.now()).setPresidencyMember("Smith")
                        .setCompanions("Doe, John").setLocation("office").setAppointmentType("Ministering").setStage("Set"),
                new Appointment().setTime(LocalDateTime.now()).setPresidencyMember("Jones")
                        .setCompanions("Curie, Sam").setLocation("office").setAppointmentType("Ministering").setStage("Set"));
        AppointmentAdapter adapter = new AppointmentAdapter(mock(FragmentActivity.class), appointments, mock(RecyclerView.class));

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void setAppointments_replacesUnderlyingList() {
        AppointmentAdapter adapter = new AppointmentAdapter(mock(FragmentActivity.class), Arrays.asList(), mock(RecyclerView.class));
        assertEquals(0, adapter.getItemCount());

        adapter.setAppointments(Arrays.asList(
                new Appointment().setTime(LocalDateTime.now()).setPresidencyMember("Smith")
                        .setCompanions("Doe, John").setLocation("office").setAppointmentType("Ministering").setStage("Set")));

        assertEquals(1, adapter.getItemCount());
    }

    private AppointmentViewHolder bindSingleAppointment(Appointment appointment) {
        AppointmentAdapter adapter = new AppointmentAdapter(mock(FragmentActivity.class), Arrays.asList(appointment), mock(RecyclerView.class));
        TextView textView = new TextView(context);
        AppointmentViewHolder holder = new AppointmentViewHolder(textView);
        adapter.onBindViewHolder(holder, 0);
        return holder;
    }

    private int backgroundColor(AppointmentViewHolder holder) {
        assertTrue("expected the item view's background to be a solid color",
                holder.textView.getBackground() instanceof ColorDrawable);
        return ((ColorDrawable) holder.textView.getBackground()).getColor();
    }
}
