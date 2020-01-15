package com.example.sm11_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.book_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchBooksData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void fetchBooksData(String query) {
        String finalQuery = prepareQuery(query);
        BookService bookService = RetrofitInstance.getRetrofitInstance().create(BookService.class);

        Call<BookContainer> bookApiCall = bookService.findBooks(finalQuery);
        bookApiCall.enqueue(new Callback<BookContainer>() {
            @Override
            public void onResponse(Call<BookContainer> call, Response<BookContainer> response) {
                setupBookListView(response.body().getBookList());
            }

            @Override
            public void onFailure(Call<BookContainer> call, Throwable t) {
                Snackbar.make(findViewById(R.id.main_view),"Something went wrong... Please try later!", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void setupBookListView(List<Book> bookList) {
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final BookAdapter adapter = new BookAdapter();
        adapter.setBooks(bookList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private String prepareQuery(String query) {
        String[] queryParts = query.split("\\s+");
        return TextUtils.join("+", queryParts);
    }

    private boolean checkNullOrEmpty(String text) {
        return text != null && !TextUtils.isEmpty(text);
    }

    public class BookAdapter extends RecyclerView.Adapter<BookViewHolder> {
        private List<Book> books;

        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            BookViewHolder bookViewHolder = new BookViewHolder(getLayoutInflater(), parent);
            return bookViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
            if (books != null) {
                Book book = books.get(position);
                holder.bind(book);
            } else {
                Log.d("MainActivity", "No books");
            }

        }

        void setBooks(List<Book> books) {
            this.books = books;
            notifyDataSetChanged();
        }



        @Override
        public int getItemCount() {
            return books.size();
        }


    }

    public class BookViewHolder extends RecyclerView.ViewHolder {
        private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";
        TextView bookTitleTextView;
        TextView bookAuthorTextView;
        ImageView bookCover;

        public BookViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.book_list_item, parent, false));
            bookTitleTextView = itemView.findViewById(R.id.book_title);
            bookAuthorTextView = itemView.findViewById(R.id.book_author);
            bookCover = itemView.findViewById(R.id.img_cover);

        }

        public void bind(Book book) {
            if(book != null && checkNullOrEmpty(book.getTitle()) && book.getAuthors() != null){
                bookTitleTextView.setText(book.getTitle());
                bookAuthorTextView.setText(TextUtils.join(", ", book.getAuthors()));
                if(book.getCover() != null){
                    Picasso.with(itemView.getContext())
                            .load(IMAGE_URL_BASE + book.getCover() + "-S.jpg")
                            .placeholder(R.drawable.ic_library_books_black_24dp).into(bookCover);
                }
                else
                {
                    bookCover.setImageResource(R.drawable.ic_library_books_black_24dp);
                }
            }
        }
    }
}






