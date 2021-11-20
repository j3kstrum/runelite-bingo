package com.runelitebingo;

import com.runeliteminigame.IMiniGamePlugin;
import com.runeliteminigame.tasks.CombatTask;
import com.runeliteminigame.tasks.CombatTaskElement;
import com.runeliteminigame.tasks.IRunescapeTask;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Random;

public class BingoConstraint {
    // TODO: Create the bingo constraint class. Constrains bingo to adhere to only combat tasks,
    // tasks with a certain duration, only skilling tasks, etc...

    IRunescapeTask[][] createTasks(IMiniGamePlugin plugin) {
        throw new NotImplementedException("Constraints are not yet supported.");
    }

    static IRunescapeTask[][] randomTasks(IMiniGamePlugin plugin) {
        IRunescapeTask[][] tasks = new IRunescapeTask[5][5];
        for (int i = 0; i < tasks.length; i++) {
            for (int j = 0; j < tasks[i].length; j++) {
                IRunescapeTask task;
                int taskType = new Random().nextInt(1);
                if (taskType < 1) {
                    CombatTaskElement[] candidates = CombatTaskElement.values();
                    CombatTaskElement selected = candidates[new Random().nextInt(candidates.length)];
                    int quantity = 1 + new Random().nextInt(selected.maxQuantity());
                    task = new CombatTask(selected.getName(), quantity, plugin);
                }
                else {
                    throw new NotImplementedException("Invalid task type selected when creating random task.");
                }
                tasks[i][j] = task;
            }
        }
        return tasks;
    }
}
