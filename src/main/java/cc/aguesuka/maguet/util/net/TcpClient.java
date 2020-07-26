package cc.aguesuka.maguet.util.net;


import java.io.Closeable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Tcp client
 *
 * @param <T> type of setting
 * @author agueuska
 */
public interface TcpClient<T extends TcpClient.Setting> extends Closeable {
    /**
     * Empty byte buffer
     */
    ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0).flip();

    /**
     * Creates an instance of TcpClient
     *
     * @param eventLoop EventLoop
     * @param setting   setting
     * @param <T>       type of setting
     * @return instance of TcpClient
     * @throws NullPointerException any null argument
     */
    static <T extends TcpClient.Setting> TcpClient<T> of(EventLoop eventLoop, T setting) {
        return new TcpClientImpl<>(eventLoop, setting);
    }

    /**
     * Connects to remote server
     *
     * @param address  remote address
     * @param callback callback on connect success
     */
    void connect(SocketAddress address, Consumer<T> callback);

    /**
     * When connect is readable, reads by buffer, invokes this callback until {@code buffer.position >= targetPosition}
     *
     * @param buffer         nonnull buffer
     * @param targetPosition the target position， {@code 0 < targetPosition <= buffer.limit()}
     * @param callback       nonnull callback
     * @throws IllegalArgumentException when {@code  requireSize <=0 || requireSize > buffer.limit()}
     * @throws IllegalStateException    not connect or is closed
     * @throws NullPointerException     any null argument
     */
    void read(ByteBuffer buffer, int targetPosition, Consumer<T> callback);

    /**
     * Binds buffer to this TcpClient, when connect is writeable and has remaining, writes it.
     *
     * @param buffer nonnull buffer
     * @throws NullPointerException  any null argument
     * @throws IllegalStateException not connect or is closed
     */
    void setWriteBuffer(ByteBuffer buffer);

    /**
     * Sets the callback, it will invoke at {@link #setWriteBuffer(ByteBuffer) writeBuffer} has not remaining
     *
     * @param callback nullable callback
     * @throws IllegalStateException not connect or is closed
     */
    void onWriteComplete(Consumer<T> callback);

    /**
     * Returns true if this closed
     *
     * @return true if this closed
     */
    boolean isClosed();

    /**
     * Closes this client
     */
    @Override
    void close();

    /**
     * The setting of client
     */
    interface Setting {
        /**
         * Invokes when client closed
         */
        void onClose();

        /**
         * Async throwable will redirect to this method
         *
         * @param throwable throwable
         */
        void handleThrowable(Throwable throwable);

        /**
         * Invoke when client selected
         */
        void onSelected();

        /**
         * Client will be closed when not set read buffer or write buffer if this returns true
         *
         * @return is auto close on idle
         */
        default boolean autoCloseOnIdle() {
            return true;
        }

        /**
         * Invokes on read or write result is EOF
         *
         * @see #autoCloseOnEof()
         */
        void onEOF();

        /**
         * If returns true, client will be closed before {@link #onEOF()}
         *
         * @return is auto close on EOF
         * @see #onEOF()
         */
        default boolean autoCloseOnEof() {
            return true;
        }
    }
}
