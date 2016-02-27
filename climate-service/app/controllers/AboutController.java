package controllers;

import play.mvc.*;
import views.html.*;


public class AboutController extends Controller {

    public static Result aboutUs() {
    	return ok(aboutUs.render());
    }

		public static Result scienceUseCase() {
    	return ok(aboutUs.render());
    }


}
