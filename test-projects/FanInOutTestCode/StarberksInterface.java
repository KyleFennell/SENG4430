import java.util.Scanner;
public class StarberksInterface{
	private void run(){
		boolean validName = false;
		//Strings for changing and calling functions needing a name
		String name = "", choiceDel, newName, delProduct, choiceChangeProduct;
		// ints for misc including switch statements
		int propertyBeingChanged, choice = 0;
		// variables for a editing a value
		float newValue;
		int newDemandValue;
		// variables for product data
		float setupCost, sellingPrice, unitCost, inventoryCost;
		int weeks, demandRate;
		//instantiating up the store object
		Store store = new Store();
		//instantiating and intialising scanner (console)
		Scanner console = new Scanner(System.in);
		do {
			System.out.println("What would you like to do?");
			System.out.println("1. Input data for one product");
			System.out.println("2. Show data from one product");
			System.out.println("3. Show the replenishment strategy for a product");
			System.out.println("4. Exit program");
			choice = console.nextInt();
			switch (choice){
				
		/* this case firstly asks for a name, checks if it between 3 and 10 characters, then checks if it matches a product.
		if the name mathes a product then the product will be able to be edited. editing is broken into 3 sections based on 
		the variable type (String, float or int). if the name doesnt match a product it will check if there is a vacant product
		ojbect. if there is then the user will be asked for all the product information. the product will be instantiated 
		and the EOQ checked. if the EOQ is higher than the demand then the user will be informed that the product is not profitable
		and the product will be deleted. the user will be asked to reenter information. if there are no vacant products then the
		user will be informed and asked if they would like to delete a product.		***this case is really long because it takes all 
		the user input*** */

				case 1:
				//checking name length is between 3 and 10
				while (validName == false){
					System.out.println("Product name:");
					console.nextLine();
					name = console.nextLine();
					name = name.toLowerCase();
					if (name.length() >= 2 && name.length() <= 9){
						break;
					}
					else{
						System.out.print("Invalid name. Try again. ");
					}
				}
				//checking if the product already exists and printing info if it does.
				//then asking if you would like to edit the data.
				if (store.checkName(name) != 4){
					store.printInfo(store.checkName(name));
					System.out.println("would you like to change anything? (y/n)");
					choiceChangeProduct = console.next();
					choiceChangeProduct = choiceChangeProduct.toLowerCase();
					if (choiceChangeProduct.equals("y")){
						do{
							System.out.println("What would you like to change?");
							System.out.println("1. Name");
							System.out.println("2. Setup cost");
							System.out.println("3. Unit cost");
							System.out.println("4. Inventory cost");
							System.out.println("5. Sellig price");
							System.out.println("6. Demand rate");
							System.out.println("7. Stop editing");
							propertyBeingChanged = console.nextInt();
							if (propertyBeingChanged != 7){
								System.out.println("What would you like to change it to?");
							}
							//change name is seperate because it is a string
							if (propertyBeingChanged == 1){
								//celaring the console
								console.nextLine();
								newName = console.nextLine();
								newName = newName.toLowerCase();
								//saking in the new name and the number of the product being edited.
								store.editName(newName, store.checkName(name));
							}
							// change other property except demand (all floats)
							else if (propertyBeingChanged < 6){
								newValue = console.nextFloat();
								// passing the ID of the property being changed, the product thats being
								// changed and the new value.
								store.editProduct(propertyBeingChanged, newValue, store.checkName(name));
							}
							else if (propertyBeingChanged == 6){
								newDemandValue = console.nextInt();
								// passing in the new value and product ID
								// doing this seperatly as it is an int and needs to be an int for ***
								store.editProductDemand(newDemandValue, store.checkName(name));
							}
						} while (propertyBeingChanged != 7);
					}
				}
				// the database is full so you can delete a product
				else if (store.getProductCount() >= 3){
					System.out.println("Product database full.");
					System.out.println("Would you like to delete a product? (y/n)");
					choiceDel = console.next();
					if (choiceDel.equals("y")){
						System.out.println("Select a number to delete: ");
						System.out.println(store.getName(1));
						System.out.println(store.getName(2));
						System.out.println(store.getName(3));
						System.out.println("Enter an invalid product to cancel.");
						console.nextLine();
						delProduct = console.nextLine();
						//checking which product has that name
						if (store.checkName(delProduct) < 4){
							//resets(deletes) the product.
							store.deleteProduct(store.checkName(delProduct));
						}
						else{
							System.out.println("Invalid product, canceling deletion.");
						}
					}
				}
				//product doesnt exist and there is room in the database so creating a new product and checking all values
				//are positive.
				else {
					do{
						//continues to ask for input for for each until value is positive
						do {
							System.out.println("Setup cost:");
							setupCost = console.nextFloat();
							if (setupCost < 0){
								System.out.println("Invalid input. Please enter a positive number.");
							}						
						} while (setupCost <= 0);
						do {
							System.out.println("Unit cost:");
							unitCost = console.nextFloat();
							if (unitCost < 0){
								System.out.println("Invalid input. Please enter a positive number.");
							}
						} while (unitCost < 0);
						do {
							System.out.println("Inventory cost:");
							inventoryCost = console.nextFloat();
							if (inventoryCost < 0){
								System.out.println("Invalid input. Please enter a positive number.");
							}
						} while (inventoryCost < 0);
						do {
							System.out.println("Selling price:");
							sellingPrice = console.nextFloat();
							if (sellingPrice < 0){
								System.out.println("Invalid input. Please enter a positive number.");
							}
						} while (sellingPrice < 0);
						do {
							System.out.println("Demand rate:");
							demandRate = console.nextInt();
							if (demandRate < 0){
								System.out.println("Invalid input. Please enter a positive number.");
							}
						} while (demandRate < 0);
						//instantiating produc and checking the EOQ is greater than demand
						store.instantiateProduct(name, setupCost, unitCost, inventoryCost, sellingPrice, demandRate);
						if (demandRate > store.getEOQ(store.checkName(name))){
							System.out.println("Product is not economically viable.");
							store.deleteProduct(store.checkName(name));
						}
					} while (demandRate > store.getEOQ(store.checkName(name)));
				}
				break;
				
		/* this case simply asks for a name, checks if it relates to a product then prints its information 
		*/

				case 2:
				//Displaying information for a product
				System.out.println("Which product would you like to see?");
				console.nextLine();
				name = console.nextLine();
				name = name.toLowerCase();
				//checking which product has that name
				if (store.checkName(name) != 4){
					//printing the products info
					store.printInfo(store.checkName(name));
				}
				else{
					System.out.println("Product doesnt exist.");
				}
				break;

		/* this case takes a name and checks which product it belongs to. it then asks for the number
		of weeks, calculates and prints out its replenishment strategy and profit at the end */

				case 3:
				if (store.getProductCount() >= 1){
					console.nextLine();
					do {
						System.out.println("Enter the name of the product: ");
						name = console.nextLine();
						name = name.toLowerCase();
						//checking which product has that name
						if (store.checkName(name) != 4){
							System.out.println("Enter number of weeks:");
							weeks = console.nextInt();
							System.out.println("The replenishment strategy for "+name+" is:");
							store.printRepStrat(weeks, store.checkName(name));
						}
						else{
							System.out.println("Invalid product name");
						}
					} while (store.checkName(name) == 4);
				}
				else{
					System.out.println("No products in the system");
				}
				break;
				
		/* this case prints out the most profitable product based on the that have had replenishment
		strategies made for them then exits the program */

				case 4:
				if (store.getEOQCount() > 0){
					store.getMostProfitable();
				}
				else {
					System.out.println("No replenishment strategies implemented");
				}
				break;
				
				default: System.out.println("Invalid imput");
				break;
			}
		} while (choice != 4);
	}
	
	public static void main(String[] args){
		StarberksInterface intFace = new StarberksInterface();
		intFace.run();
	}
}