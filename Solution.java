// Author: Berk Saltuk Yilmaz
// This program includes blood, sweat, and tears...

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

interface TaskState{
	public TaskState complete();
	public TaskState inProgress();
}

class InProgressTaskState implements TaskState{
	
	private static InProgressTaskState instance;
	
	private InProgressTaskState(){}
	
	public static InProgressTaskState getInstance() {
		if(instance == null) {
			instance = new InProgressTaskState();
		}
		return instance;
	}
	
	@Override
	public TaskState complete() {
		
		return CompletedTaskState.getInstance();
	}

	@Override
	public TaskState inProgress() {
		return this;
	}
	
	@Override
	public String toString() {
		return "In Progress";
	}
	
}

class CompletedTaskState implements TaskState{

	public static CompletedTaskState instance;
	
	public CompletedTaskState() {}
	
	public static CompletedTaskState getInstance() {
		if(instance == null) {
			instance = new CompletedTaskState();
		}
		return instance;
	}
	
	@Override
	public TaskState complete() {
		return this;
	}

	@Override
	public TaskState inProgress() {
		return this;
	}
	
	@Override
	public String toString() {
		return "Completed";
	}
	
}

class CreatedTaskState implements TaskState{

	private static CreatedTaskState instance;
	
	public CreatedTaskState() {}
	
	public static CreatedTaskState getInstance() {
		if( instance == null) {
			instance = new CreatedTaskState();
		}
		return instance;
	}
	
	@Override
	public TaskState complete() {
		
		return CompletedTaskState.getInstance();
	}

	@Override
	public TaskState inProgress() {
		return InProgressTaskState.getInstance();
	}
	
	@Override
	public String toString() {
		return "Created";
	}
}

interface TaskComponent{
	
}

interface ListSort{
	public ArrayList<TaskComponent> sortList(ArrayList<TaskComponent> list, TaskComponent el);
}

class SortAddedOrder implements ListSort{

	@Override
	public ArrayList<TaskComponent> sortList(ArrayList<TaskComponent> list, TaskComponent el) {
		
		if(el instanceof Task) list.add(0, el);
		else list.add(list.size(), el);
		
		return list;
	}
	public String toString(){
		return " [Add Order]";
	}
}

class SortAlphabeticalOrder implements ListSort{
	private ArrayList<TaskComponent> temp;
	@Override
	public ArrayList<TaskComponent> sortList(ArrayList<TaskComponent> list, TaskComponent el) {
		temp = new ArrayList<TaskComponent>();
		
		list.add(el);
		
		for(int i = 0; i < list.size(); i++) {
			if( list.get(i) instanceof Task) temp.add(list.get(i));
		}
			
		Collections.sort(temp, new Comparator<TaskComponent>(){
			public int compare(TaskComponent t1, TaskComponent t2) {
				return ((Task) t1).getDescription().compareTo(((Task) t2).getDescription());
			}
		});
		
		
		for(int i = 0; i < list.size(); i++) {
			if( list.get(i) instanceof List) temp.add(temp.size(), list.get(i));
		}
		
		list = new ArrayList<TaskComponent>();
		for( int i = 0; i < temp.size(); i++) {
			list.add(temp.get(i));
		}
		return list;
	}
	public String toString(){
		return " [Alphabetical Order]";
	}
}

class SortTargetDateOrder implements ListSort{

	private ArrayList<TaskComponent> temp;
	@Override
	public ArrayList<TaskComponent> sortList(ArrayList<TaskComponent> list, TaskComponent el) {
		temp = new ArrayList<TaskComponent>();
		
		list.add(el);
		
		for(int i = 0; i < list.size(); i++) {
			if( list.get(i) instanceof Task) temp.add(list.get(i));
		}
			
		Collections.sort(temp, new Comparator<TaskComponent>(){
			public int compare(TaskComponent t1, TaskComponent t2) {
				return (int) (((Task) t1).getTargetDate().getTime() - ((Task) t2).getTargetDate().getTime());
			}
		});
		
		
		for(int i = 0; i < list.size(); i++) {
			if( list.get(i) instanceof List) temp.add(temp.size(), list.get(i));
		}
		
		list = new ArrayList<TaskComponent>();
		for( int i = 0; i < temp.size(); i++) {
			list.add(temp.get(i));
		}
		return list;
	}
	
	public String toString(){
		return " [Target Date Order]";
	}
	
}

class List implements TaskComponent{
	private ArrayList<TaskComponent> taskComponent;
	private ListSort strategy;
	private String description;
	
	public List(String description, ListSort strategy) {
		this.description = description;
		taskComponent = new ArrayList<TaskComponent>();
		this.strategy = strategy;

	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void add(TaskComponent t) {

		taskComponent = strategy.sortList(taskComponent, t);
		
	}
	
	public String displayTaskComponents() {
		String str = " {\n";
		for(int i = 0 ; i < taskComponent.size(); i++) {
			str += taskComponent.get(i).toString() + "\n";
		}
		str += "}";
		
		return str;
	}

	public String toString() {
		 return this.getDescription() + this.strategy.toString()  + displayTaskComponents();
		
	}
}


interface Task extends TaskComponent{
	public abstract void complete();
	public abstract void inProgress();
	public String getDescription();
	public Date getTargetDate();
	public TaskState getStatus();
}

abstract class TaskDecorator implements Task{
	protected Task task;
}

class ElapsedTime extends TaskDecorator{
	private Date timeCreated;
	
	public ElapsedTime(Task task, Date timeCreated) {
		this.task = task;
		this.timeCreated = timeCreated;
	}
	
	public long calculateElapsedTime()
	{
		Date now = new Date(); 
		long elapsedDays = now.getTime() - this.timeCreated.getTime();
		return TimeUnit.DAYS.convert(elapsedDays, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void complete() {
		this.task.complete();
		
	}

	@Override
	public void inProgress() {
		this.task.inProgress();
		
	}

	@Override
	public String getDescription() {
		return this.task.getDescription();
	}

	@Override
	public Date getTargetDate() {
		return this.task.getTargetDate();
	}

	@Override
	public TaskState getStatus() {
		return this.task.getStatus();
	}
	
	public String toString() {
		String s = this.task.toString();
		s += "[Elapsed time: " + this.calculateElapsedTime() + " day(s)]";
		return s;
	}
}

class TrackHistory extends TaskDecorator{
	private ArrayList<TaskState> history;
	
	public TrackHistory(Task task) {
		this.task = task;
		history = new ArrayList<TaskState>();
		history.add(this.task.getStatus());
	}
	@Override
	public void complete() {
		if(!(this.task.getStatus().toString().equals(this.task.getStatus().complete().toString())))
		{
			history.add(this.task.getStatus().complete());
		}
		this.task.complete();
	}

	@Override
	public void inProgress() {
		if(!(this.task.getStatus().toString().equals(this.task.getStatus().inProgress().toString())))
		{
			history.add(this.task.getStatus().inProgress());
		}
		this.task.inProgress();
		
	}

	@Override
	public String getDescription() {
		return this.task.getDescription();
	}

	@Override
	public Date getTargetDate() {
		return this.task.getTargetDate();
	}

	@Override
	public TaskState getStatus() {
		return this.task.getStatus();
	}
	
	public String toString() {
		String s = this.task.toString();
		s+= " [Status History: ";
		for(int i = 0; i < history.size(); i++) {
			s += history.get(i).toString();
			if( i != history.size()-1)
			{
				s+= "->";
			}
			
		}
		s += "]"; 
		return s;
	}
	
}

class PlainTask implements TaskComponent, Task{
	private String description;
	private Date targetDate;
	private TaskState status;
	//private boolean trackHistory;
	//private boolean trackElapsedTime;
	//private ArrayList<TaskState> history;
	
	public PlainTask(TaskState status, String description, Date targetDate) {
		this.description = description;
		this.targetDate = targetDate;
		this.status = status;
	}
	
	public void complete() {

		this.status = this.status.complete();
	}
	
	public void inProgress() {

		this.status = this.status.inProgress();
	}
	
	public String getDescription() {
		return description;
	}

	public Date getTargetDate() {
		return targetDate;
	}
	
	public TaskState getStatus() {
		return this.status;
	}
	
	public String toString() {
		String s = "-" + this.getDescription() + " " + (new SimpleDateFormat("yyyy-MM-dd").format(this.getTargetDate())) + " ["+this.getStatus()+"]";

		return s;
	}

}



public class Solution {
	
	public static void main(String [] args) throws ParseException {
		//Task t = new Task(new CreatedTaskState(), "Fix Lights", new SimpleDateFormat("yyyyMMdd").parse("20220428"), true, true);
		Task fixLight = new PlainTask(new CreatedTaskState(), "Fix Lights", new SimpleDateFormat("yyyyMMdd").parse("20220522"));
		List toDo = new List("My Todos", new SortAddedOrder());
		fixLight.inProgress();
		Task attendSeminar = new PlainTask(new CreatedTaskState(), "Attend Seminar", new SimpleDateFormat("yyyyMMdd").parse("20220510"));
		toDo.add(attendSeminar);
		
		toDo.add(fixLight);
		
		List cs319 = new List("CS 319", new SortTargetDateOrder());
		
		Task prepIter = new PlainTask(new CreatedTaskState(), "Prepare iteration 1 reports", new SimpleDateFormat("yyyyMMdd").parse("20220410"));
		prepIter.complete();
		Task addressFeedback = new ElapsedTime(new PlainTask(new CreatedTaskState(), "Address TA/Instructor feedback", new SimpleDateFormat("yyyyMMdd").parse("20220502")), new Date());
		cs319.add(addressFeedback);
		cs319.add(prepIter);
		
		Task subDes = new PlainTask(new CreatedTaskState(), "Submit design patterns HW", new SimpleDateFormat("yyyyMMdd").parse("20220426"));
		subDes.inProgress();
		cs319.add(subDes);
		
		List implementation = new List("Implementation", new SortTargetDateOrder());
		
		Task impFE = new TrackHistory(new ElapsedTime(new PlainTask(new CreatedTaskState(), "Implement front-end components", new SimpleDateFormat("yyyyMMdd").parse("20220501")), new Date()));
		impFE.inProgress();
		implementation.add(impFE);
		
		Task defClass = new TrackHistory( new PlainTask(new CreatedTaskState(), "Define classes", new SimpleDateFormat("yyyyMMdd").parse("20220420")));
		defClass.inProgress();
		defClass.complete();
		implementation.add(defClass);
		
		Task designBE = new PlainTask(new CreatedTaskState(), "Design backend APIs", new SimpleDateFormat("yyyyMMdd").parse("20220430"));
		designBE.inProgress();
		implementation.add(designBE);
		
		cs319.add(implementation);
		
		toDo.add(cs319);
		
		
		List grocery = new List("Grocery", new SortAddedOrder());
		List fruit = new List("Fruits", new SortAlphabeticalOrder());
		List dairy = new List("Dairy", new SortAddedOrder());
		
		toDo.add(grocery);
		grocery.add(fruit);
		grocery.add(dairy);
		
		Task apple = new PlainTask(new CreatedTaskState(), "Apples", new SimpleDateFormat("yyyyMMdd").parse("20220427"));
		Task banana = new PlainTask(new CreatedTaskState(), "Bananas", new SimpleDateFormat("yyyyMMdd").parse("20220425"));
		Task orange = new PlainTask(new CreatedTaskState(), "Orange", new SimpleDateFormat("yyyyMMdd").parse("20220422"));
		
		fruit.add(orange);
		orange.complete();
		banana.complete();
		fruit.add(banana);
		fruit.add(apple);
		
		
		Task milk = new PlainTask(new CreatedTaskState(), "Milk", new SimpleDateFormat("yyyyMMdd").parse("20220429"));
		Task yoghurt = new PlainTask(new CreatedTaskState(), "Apples", new SimpleDateFormat("yyyyMMdd").parse("20220423"));
		dairy.add(yoghurt);
		dairy.add(milk);
		milk.complete();
		System.out.println(toDo);

	}
	
}
