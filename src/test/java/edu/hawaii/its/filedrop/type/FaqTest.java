package edu.hawaii.its.filedrop.type;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class FaqTest {

    Faq faq;

    @Before
    public void setUp() {
        faq = new Faq();
    }

    @Test
    public void accessors() {
        assertThat(faq.getId(), equalTo(null));
        assertThat(faq.getQuestion(), equalTo(null));
        assertThat(faq.getAnswer(), equalTo(null));

        faq.setId(999);
        faq.setQuestion("question");
        faq.setAnswer("answer");
        assertThat(faq.getId(), equalTo(999));
        assertThat(faq.getQuestion(), equalTo("question"));
        assertThat(faq.getAnswer(), equalTo("answer"));
    }

    @Test
    public void testToString() {
        assertThat(faq.toString(), startsWith("Faq ["));
        assertThat(faq.toString(), containsString("id=null, "));
        assertThat(faq.toString(), containsString(", question=null"));
        assertThat(faq.toString(), containsString(", answer=null"));

        faq.setId(999);
        assertThat(faq.toString(), startsWith("Faq [id=999"));

        faq.setQuestion("question2");
        assertThat(faq.toString(), containsString(", question=question2"));

        faq.setAnswer("answer2");
        assertThat(faq.toString(), containsString(", answer=answer2"));
    }

    @Test
    public void testEquals() {
        Faq f0 = new Faq();
        assertThat(f0, equalTo(f0));
        assertThat(f0, not(equalTo(new String())));
        assertFalse(f0.equals(null));
        Faq f1 = new Faq();
        assertThat(f0, equalTo(f1));
        assertThat(f1, equalTo(f0));

        f0.setId(1);
        assertThat(f0, not(equalTo(f1)));
        assertThat(f1, not(equalTo(f0)));
        f1.setId(1);
        assertThat(f0, equalTo(f1));
        assertThat(f1, equalTo(f0));
    }

    @Test
    public void testHashCode() {
        Faq f0 = new Faq();
        Faq f1 = new Faq();

        assertThat(f0, equalTo(f1));
        assertThat(f1, equalTo(f0));
        assertThat(f0.hashCode(), equalTo(f1.hashCode()));
        assertThat(f1.hashCode(), equalTo(f0.hashCode()));

        f0.setId(1);
        assertThat(f0.hashCode(), not(equalTo(f1.hashCode())));
        assertThat(f1.hashCode(), not(equalTo(f0.hashCode())));
        f1.setId(f0.getId());
        assertThat(f0.hashCode(), equalTo(f1.hashCode()));
        assertThat(f1.hashCode(), equalTo(f0.hashCode()));

        f0.setQuestion("test question");
        assertThat(f0.hashCode(), not(equalTo(f1.hashCode())));
        assertThat(f1.hashCode(), not(equalTo(f0.hashCode())));
        f1.setQuestion(f0.getQuestion());
        assertThat(f0.hashCode(), equalTo(f1.hashCode()));
        assertThat(f1.hashCode(), equalTo(f0.hashCode()));

        f0.setAnswer("test answer");
        assertThat(f0.hashCode(), not(equalTo(f1.hashCode())));
        assertThat(f1.hashCode(), not(equalTo(f0.hashCode())));
        f1.setAnswer(f0.getAnswer());
        assertThat(f0.hashCode(), equalTo(f1.hashCode()));
        assertThat(f1.hashCode(), equalTo(f0.hashCode()));
    }
}
