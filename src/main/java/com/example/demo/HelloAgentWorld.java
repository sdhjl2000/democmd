package com.example.demo;

import java.lang.instrument.Instrumentation;

/**
 * Created by sdhjl2000 on 2017/11/4.
 */
public class HelloAgentWorld {

        public static void agentmain(String agentArgs, Instrumentation inst)
        {
            System.out.println(agentArgs);
            System.out.println("Hi from the agent!");
            System.out.println("I've got instrumentation!: " + inst);
        }


}
