INSTRUCTIONS FOR USING THE OPEN EDIT FRAMEWORK

1.  The Open Edit framework is a web application development framework.
	 


2.  Obtain the OpenEdit web archive (.war) files

	You should use the .war files on your OpenEdit CD.  Otherwise, the very
	latest OpenEdit build can be downloaded from the Anthill build system at
	http://projects.einnovation.com/anthill.  The projects of interest are:

	The main example web site can be found here:
	
	http://projects.einnovation.com/anthill/projects/openedit-acme/ROOT.war 
	(rename to openedit-acme.war)

	If you already have web content that would want to edit we recomment you download only the editor itself.

	http://projects.einnovation.com/anthill/projects/openedit-editor/ROOT.war (rename to openedit-editor.war)

	You may also be curious about the openedit-project directory.  This contains
	a stub that can be used as a starting point for a new OpenEdit project or
	add on.


3.  Deploy the .war files

	Copy the .war files to the webapps folder of your web server (e.g. C:\Program Files\Apache Software Foundation\Tomcat 5.0\webapps).  Your web server should extract the contents of the .war files automatically and make OpenEdit available to clients.


4.  Access OpenEdit through your web browser.

	Open your web browser and locate the OpenEdit directory on the machine where your web server is running (e.g. http://localhost:8080/openedit-editor/).  Log in and begin using the product.  Or, to try the Acme demo, browse to that directory (e.g. http://localhost:8080/openedit-acme/).

5. If you have an existing HTML application there is an install.xml ant task you can run from within your existing WEB-INF directory.