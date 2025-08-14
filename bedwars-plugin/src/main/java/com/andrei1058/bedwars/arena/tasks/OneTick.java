
package com.andrei1058.bedwars.arena.tasks;


import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.arena.OreGenerator;

public class OneTick implements Runnable {

    private final IGenerator generator;

    public OneTick(IGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void run() {
        if (generator instanceof OreGenerator) {
            ((OreGenerator) generator).enableRotation();
        }
    }
}
