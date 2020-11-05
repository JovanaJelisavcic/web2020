package services;



import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;










import beans.User;
import dao.ApartmentDAO;
import dao.UserDAO;

@Path("")
public class LoginService {
	
	@Context
	ServletContext ctx;
	
	public LoginService() {
		
	}
	
	@PostConstruct
	// ctx polje je null u konstruktoru, mora se pozvati nakon konstruktora (@PostConstruct anotacija)
	public void init() {
		// Ovaj objekat se instancira vi�e puta u toku rada aplikacije
		// Inicijalizacija treba da se obavi samo jednom
		
		if (ctx.getAttribute("userDAO") == null) {
	    	String contextPath = ctx.getRealPath("/");
			ctx.setAttribute("userDAO", new UserDAO(contextPath));
		}
		if (ctx.getAttribute("apartmentDAO") == null) {
	    	String contextPath = ctx.getRealPath("/");
			ctx.setAttribute("apartmentDAO", new ApartmentDAO(contextPath));
		}
	}
	
	
	
	    @POST
	    @Path("login")
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response getMsg(User user,@Context HttpServletRequest request)
	    {
	    
	    	UserDAO userDao = (UserDAO) ctx.getAttribute("userDAO");
			User loggedUser = userDao.find(user.getUsername(), user.getPassword());
			if (loggedUser == null) {
				return Response.status(400).entity("Invalid username and/or password").build();
			}
			request.getSession().setAttribute("user", loggedUser);
			return Response.status(200).build();
	    }
	
	
	
	@POST
	@Path("/logout")
	@Consumes(MediaType.APPLICATION_JSON)
	public void logout(@Context HttpServletRequest request) {
		request.getSession().invalidate();
	}
	
	@GET
	@Path("/currentUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User login(@Context HttpServletRequest request) {
		return (User) request.getSession().getAttribute("user");
	}
	

	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(User user,
			@Context HttpServletRequest request) {
		UserDAO userDao = (UserDAO) ctx.getAttribute("userDAO");		
		if (!userDao.checkUnique(user.getUsername())) {
			return Response.status(400).entity("Username already taken!").build();
		}
		user.setRole("guest");
		if(!userDao.saveUser(user)) {
			return Response.status(400).entity("Registration unsuccessful").build();			
		}
		request.getSession().setAttribute("user", user);
		return Response.status(200).build();
	}
	
	@POST
	@Path("/changeProfile")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changeProfile(User user, @Context HttpServletRequest request) {
		User oldInfo =(User) request.getSession().getAttribute("user");	
		UserDAO userDao = (UserDAO) ctx.getAttribute("userDAO");
		if (user.getPassword().equals(oldInfo.getPassword())) { 
			if(!userDao.changeUser(user)) {
				return Response.status(400).entity("Saving changes unsuccessful").build();			
			}else{
			request.getSession().setAttribute("user", user);
			return Response.status(200).build();}		
		}else
			return Response.status(400).entity("Wrong password!").build();
		
	}
}

