package de.tum.in.tumcampusapp.models;

import android.content.Context;

import com.squareup.okhttp.CertificatePinner;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;

import de.tum.in.tumcampusapp.auxiliary.AuthenticationManager;
import de.tum.in.tumcampusapp.auxiliary.Const;
import retrofit.Callback;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public class TUMCabeClient {

    private static final String API_HOSTNAME = Const.API_HOSTNAME;
    private static final String API_BASEURL = "/Api";
    private static final String API_CHAT = "/chat/";
    private static final String API_CHAT_ROOMS = API_CHAT + "rooms/";
    private static final String API_CHAT_MEMBERS = API_CHAT + "members/";
    private static final String API_SESSION = "/session/";
    private static final String API_NEWS = "/news/";
    private static final String API_MENSA = "/mensen/";
    private static final String API_CURRICULA = "/curricula/";
    private static final String API_REPORT = "/report/";
    private static final String API_STATISTICS = "/statistics/";
    private static final String API_CINEMA = "/kino/";
    private static final String API_NOTIFICATIONS = "/notifications/";
    private static final String API_LOCATIONS = "/locations/";
    private static final String API_DEVICE = "/device/";
    private static final String API_QUESTION = "/question/";
    private static final String API_ANSWER_QUESTION = "/question/answer/";
    private static final String API_OWN_QUESTIONS = "/question/my/";
    private static final String API_FACULTY = "/faculty/";


    private static TUMCabeClient instance;
    private TUMCabeAPIService service;

    private TUMCabeClient(final Context c) {
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("X-DEVICE-ID", AuthenticationManager.getDeviceID(c));
            }
        };
        //Pin our known fingerprints, which I retrieved on 28. June 2015
        final CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add(API_HOSTNAME, "sha1/eeoui1Gne7kkDN/6HlgoxHkD18s=") //Fakultaet fuer Informatik
                .add(API_HOSTNAME, "sha1/AC508zHZltt8Aa1ZpUg5C9tMNJ8=") //Technische Universitaet Muenchen
                .add(API_HOSTNAME, "sha1/7+NhGLCLRZ1RDbncIhu3ksHeOok=") //DFN-Verein PCA Global
                .add(API_HOSTNAME, "sha1/8GO6fJoWdEqc21TsI81nKY58SU0=") //Deutsche Telekom Root CA 2
                .build();
        final OkHttpClient client = new OkHttpClient();
        client.setCertificatePinner(certificatePinner);

        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public Throwable handleError(RetrofitError cause) {
                Throwable t = cause.getCause();
                if (t instanceof SSLPeerUnverifiedException) {
                    //TODO show a error message
                    //Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
                }

                //Return the same cause, so it can be handled by other activities
                return cause;
            }
        };
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(client))
                .setEndpoint("https://" + API_HOSTNAME + API_BASEURL)
                .setRequestInterceptor(requestInterceptor)
                .setErrorHandler(errorHandler)
                .build();
        service = restAdapter.create(TUMCabeAPIService.class);
    }

    public static TUMCabeClient getInstance(Context c) {
        c.getApplicationContext();
        if (instance == null) {
            instance = new TUMCabeClient(c);
        }
        return instance;
    }

    // Fetches faculty data (facname, id).Relevant for the user to select own major in majorSpinner in WizNavStartActivity
    public ArrayList<Faculty> getFaculties() {
        return service.getFaculties();
    }

    // Deletes ownQuestion..Relevant for allowing the user to delete own questions under responses in SurveyActivity
    public void deleteOwnQuestion(int question, Callback<Question> cb) {
        service.deleteOwnQuestion(question, cb);
    }

    // Fetches users ownQuestions and responses.Relevant for displaying results on ownQuestion under responses in SurveyActivity
    public ArrayList<Question> getOwnQuestions() {
        return service.getOwnQuestions();
    }

    // Submits user's answer on a given question.Gets triggered through in the survey card.
    public void submitAnswer(Question question, Callback<Question> cb) {
        service.answerQuestion(question, cb);
    }

    // Fetches openQuestions which are relevant for the surveyCard.
    public ArrayList<Question> getOpenQuestions() {
        return service.getOpenQuestions();
    }

    // Submits user's own question. Gets triggered from the SurveyActivity
    public void createQuestion(Question question, Callback<Question> cb) {
        service.createQuestion(question, cb);
    }

    public void createRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        verification.setData(chatRoom);
        service.createRoom(verification, cb);
    }

    public ChatRoom createRoom(ChatRoom chatRoom, ChatVerification verification) {
        verification.setData(chatRoom);
        return service.createRoom(verification);
    }

    public ChatRoom getChatRoom(int id) {
        return service.getChatRoom(id);
    }

    public ChatMember createMember(ChatMember chatMember) {
        return service.createMember(chatMember);
    }

    public void leaveChatRoom(ChatRoom chatRoom, ChatVerification verification, Callback<ChatRoom> cb) {
        service.leaveChatRoom(chatRoom.getId(), verification, cb);
    }

    public ChatMessage sendMessage(int roomId, ChatMessage chatMessageCreate) {
        return service.sendMessage(roomId, chatMessageCreate);
    }

    public ChatMessage updateMessage(int roomId, ChatMessage message) {
        return service.updateMessage(roomId, message.getId(), message);
    }

    public ArrayList<ChatMessage> getMessages(int roomId, long messageId, @Body ChatVerification verification) {
        return service.getMessages(roomId, messageId, verification);
    }

    public ArrayList<ChatMessage> getNewMessages(int roomId, @Body ChatVerification verification) {
        return service.getNewMessages(roomId, verification);
    }

    public List<ChatRoom> getMemberRooms(int memberId, ChatVerification verification) {
        return service.getMemberRooms(memberId, verification);
    }

    public void getPublicKeysForMember(ChatMember member, Callback<List<ChatPublicKey>> cb) {
        service.getPublicKeysForMember(member.getId(), cb);
    }

    public void uploadRegistrationId(int memberId, ChatRegistrationId regId, Callback<ChatRegistrationId> cb) {
        service.uploadRegistrationId(memberId, regId, cb);
    }

    public GCMNotification getNotification(int notification) {
        return service.getNotification(notification);
    }

    public void confirm(int notification) {
        service.confirm(notification);
    }

    public List<GCMNotificationLocation> getAllLocations() {
        return service.getAllLocations();
    }

    public GCMNotificationLocation getLocation(int locationId) {
        return service.getLocation(locationId);
    }

    public List<String> putBugReport(BugReport r) {
        return service.putBugReport(r);
    }

    public void putStatistics(Statistics s) {
        service.putStatistics(s, new Callback<String>() {
            @Override
            public void success(String s, retrofit.client.Response response) {
                //We don't care about any responses
            }

            @Override
            public void failure(RetrofitError error) {
                //Or if this fails
            }
        });
    }

    public void deviceRegister(DeviceRegister verification, Callback<TUMCabeStatus> cb) {
        service.deviceRegister(verification, cb);
    }

    public void deviceUploadGcmToken(DeviceUploadGcmToken verification, Callback<TUMCabeStatus> cb) {
        service.deviceUploadGcmToken(verification, cb);
    }

    private interface TUMCabeAPIService {

        @GET(API_FACULTY)
        ArrayList<Faculty> getFaculties();

        @DELETE(API_QUESTION + "{question}")
        void deleteOwnQuestion(@Path("question") int question, Callback<Question> cb);

        @GET(API_OWN_QUESTIONS)
        ArrayList<Question> getOwnQuestions();

        @POST(API_ANSWER_QUESTION)
        void answerQuestion(@Body Question question, Callback<Question> cb);

        //Questions
        @POST(API_QUESTION)
        void createQuestion(@Body Question question, Callback<Question> cb);

        @GET(API_QUESTION)
        ArrayList<Question> getOpenQuestions();

        //Group chat
        @POST(API_CHAT_ROOMS)
        void createRoom(@Body ChatVerification verification, Callback<ChatRoom> cb);

        @POST(API_CHAT_ROOMS)
        ChatRoom createRoom(@Body ChatVerification verification);

        @GET(API_CHAT_ROOMS + "{room}")
        ChatRoom getChatRoom(@Path("room") int id);

        @POST(API_CHAT_ROOMS + "{room}/leave/")
        void leaveChatRoom(@Path("room") int roomId, @Body ChatVerification verification, Callback<ChatRoom> cb);

        //Get/Update single message
        @PUT(API_CHAT_ROOMS + "{room}/message/")
        ChatMessage sendMessage(@Path("room") int roomId, @Body ChatMessage message);

        @PUT(API_CHAT_ROOMS + "{room}/message/{message}/")
        ChatMessage updateMessage(@Path("room") int roomId, @Path("message") int messageId, @Body ChatMessage message);

        //Get all recent messages or older ones
        @POST(API_CHAT_ROOMS + "{room}/messages/{page}/")
        ArrayList<ChatMessage> getMessages(@Path("room") int roomId, @Path("page") long messageId, @Body ChatVerification verification);

        @POST(API_CHAT_ROOMS + "{room}/messages/")
        ArrayList<ChatMessage> getNewMessages(@Path("room") int roomId, @Body ChatVerification verification);

        @POST(API_CHAT_MEMBERS)
        ChatMember createMember(@Body ChatMember chatMember);

        @GET(API_CHAT_MEMBERS + "{lrz_id}/")
        ChatMember getMember(@Path("lrz_id") String lrzId);

        @POST(API_CHAT_MEMBERS + "{memberId}/rooms/")
        List<ChatRoom> getMemberRooms(@Path("memberId") int memberId, @Body ChatVerification verification);

        @GET(API_CHAT_MEMBERS + "{memberId}/pubkeys/")
        void getPublicKeysForMember(@Path("memberId") int memberId, Callback<List<ChatPublicKey>> cb);

        @POST(API_CHAT_MEMBERS + "{memberId}/registration_ids/add_id")
        void uploadRegistrationId(@Path("memberId") int memberId, @Body ChatRegistrationId regId, Callback<ChatRegistrationId> cb);

        @GET(API_NOTIFICATIONS + "{notification}/")
        GCMNotification getNotification(@Path("notification") int notification);

        @GET(API_NOTIFICATIONS + "confirm/{notification}/")
        String confirm(@Path("notification") int notification);

        //Locations
        @GET(API_LOCATIONS)
        List<GCMNotificationLocation> getAllLocations();

        @GET(API_LOCATIONS + "{locationId}/")
        GCMNotificationLocation getLocation(@Path("locationId") int locationId);

        //Bug Reports
        @PUT(API_REPORT)
        List<String> putBugReport(@Body BugReport r);

        //Statistics
        @PUT(API_STATISTICS)
        void putStatistics(@Body Statistics r, Callback<String> cb);

        //Device
        @POST(API_DEVICE + "register/")
        void deviceRegister(@Body DeviceRegister verification, Callback<TUMCabeStatus> cb);

        @POST(API_DEVICE + "addGcmToken/")
        void deviceUploadGcmToken(@Body DeviceUploadGcmToken verification, Callback<TUMCabeStatus> cb);
    }
}