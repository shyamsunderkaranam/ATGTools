package com.atg.atgtools.services;
/*Fork me on GitHub
https://www.codepedia.org/ama/how-to-make-parallel-calls-in-java-with-completablefuture-example
https://www.baeldung.com/async-http-client
https://www.baeldung.com/spring-webclient-simultaneous-calls
https://dzone.com/articles/high-concurrency-http-clients-on-the-jvm
https://www.google.com/search?rlz=1C1GCEA_enIN915IN915&q=Java+parallel+HTTP+requests&sa=X&ved=2ahUKEwiss4vdoIjvAhUbzjgGHVT2BpwQ1QIwCnoECAoQAQ
CodepediaOrg	CodepediaOrgFeed CategoriesTagsAboutBookmarks
How to make parallel calls in Java with CompletableFuture example
January 26, 2018
 Reading time ~1 minute

How to make parallel calls in Java with CompletableFuture example
Dev-Bookmarks Logo
(P) Bookmarks.dev - Open source Bookmarks and Code Snippets Manager for Developers & Co. See our How To guides to help you get started. Public Bookmarks Repo on Github - 

Some time ago I wrote how elegant and rapid is to make parallel calls in NodeJS with async-await and Promise.all capabilities. Well, it turns out in Java is just as elegant and succinct with the help of CompletableFuture which was introduced in Java 8. To demonstrate that let’s imagine that you need to retrieve a list of ToDos from a REST service, given their Ids. Of course you could iterate through the list of Ids and sequentially call the web service, but it’s much more performant to do it in parallel with asynchronous calls.

Here is the piece of code that might do just that:*/



import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ParallelCallsDemoService {

    @Inject
    RestApiClient restApiClient;

    public List<ToDo> getToDos(List<String> ids){

        List<CompletableFuture<ToDo>> futures =
                ids.stream()
                          .map(id -> getToDoAsync(id))
                          .collect(Collectors.toList());

        List<ToDo> result =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());

        return result;
    }


    CompletableFuture<ToDo> getToDoAsync(String id){

        CompletableFuture<ToDo> future = CompletableFuture.supplyAsync(new Supplier<ToDo>() {
            @Override
            public ToDo get() {
                final ToDo toDo = restApiClient.getToDo(id);

                return toDo;
            }
        });

        return future;
    }

}
/*COPY
Notes:

the supplyAsync method takes a Supplier which contains the code you want to execute asynchronously - in this case this is where the REST call takes place…
you fire all the rest calls and you collect all the futures in a list
in the end you collect all the results with the help of the join() method - this returns the value of the CompletableFuture when complete, or throws an (unchecked) exception if completed exceptionally.
Subscribe to our newsletter for more code resources and news
JAVAJAVAEEASYNCHRONOUS
ADRIAN MATEI
 LIKE  TWEET
About the Author
Adrian Matei
Adrian Matei
Life force expressing itself as a coding capable human being
 
 
 

Follow @CodepediaOrg
Read More
Mongo create full text index example
Code snippets showing how to create a mongo create full text index and how to search for results with its help. Continue reading

Mongo full text search example
Published on February 07, 2021
Mongoose query pagination example
Published on February 05, 2021
© 2021 CodepediaOrg. Powered by Jekyll using the Neo-HPSTR Theme.
 | Follow @CodepediaOrgTop*/