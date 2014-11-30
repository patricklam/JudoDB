package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ConfigWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final JudoDB jdb;
    private static final String PULL_CONFIG_URL = JudoDB.BASE_URL + "pull_config.php";
    //private static final String PUSH_CONFIG_URL = JudoDB.BASE_URL + "push_config.php";
    private static final String CALLBACK_URL_SUFFIX_Q = "?callback=";

    // useful URLs: http://www.filsa.net/2010/01/23/more-on-tablayoutpanel/
    // http://www.filsa.net/2010/01/21/gwt-notes-tablayoutpanel/

    // club data:
    // nom du club
    // numero (Judo QC) du club

    // montants (should theoretically be stored with the session)
    // passeport judo QC [5]
    // frais non-resident [5]
    // penalty for prorata (percent of cost) [5]

    // misc:
    // age veteran [35]

    // for each session:
    // seqno
    // abbrev [A12]
    // effective_year [2013]
    // debut_cours, fin_cours [newDate("1/Sep/2012"), newDate("11/May/2013"))]
    // liste des prix:
    //  division, 1 session, 2 sessions, judoQC

    // for each division:
    // name [Junior]
    // abbrev [U20N]
    // years_ago [20]
    // noire [true]
    // aka [U20]

    // for each cours:
    //      new Cours("0", "Adultes (LM2015-2145, V2000-2145)", "LM2015 V2000", ""),
    // seqno [0]
    // name [Adultes (LM2015-2145, V2000-2145)]
    // abbrev [LM2015 V2000]
    // entraineur
    // which sessions does the cours apply to?

    // for each escompte:
    // seqno
    // name [2e membre]
    // percent [10]

    public ConfigWidget(JudoDB jdb) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));
        jdb.pleaseWait();
    }
}
